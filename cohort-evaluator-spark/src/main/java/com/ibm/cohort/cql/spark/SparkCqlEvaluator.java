/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.util.LongAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.CqlDebug;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;
import com.ibm.cohort.cql.evaluation.CqlEvaluator;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.library.PriorityCqlLibraryProvider;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinition;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinitions;
import com.ibm.cohort.cql.spark.aggregation.ContextRetriever;
import com.ibm.cohort.cql.spark.data.DatasetRetriever;
import com.ibm.cohort.cql.spark.data.DefaultDatasetRetriever;
import com.ibm.cohort.cql.spark.data.DefaultSparkOutputColumnEncoder;
import com.ibm.cohort.cql.spark.data.SparkDataRow;
import com.ibm.cohort.cql.spark.data.SparkOutputColumnEncoder;
import com.ibm.cohort.cql.spark.data.SparkSchemaCreator;
import com.ibm.cohort.cql.spark.data.SparkTypeConverter;
import com.ibm.cohort.cql.spark.metrics.CustomMetricSparkPlugin;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import com.ibm.cohort.cql.terminology.UnsupportedTerminologyProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import com.ibm.cohort.cql.util.MapUtils;
import com.ibm.cohort.datarow.engine.DataRowDataProvider;
import com.ibm.cohort.datarow.engine.DataRowRetrieveProvider;
import com.ibm.cohort.datarow.model.DataRow;

import scala.Tuple2;

/**
 * Given knowledge and configuration artifacts available under local storage and
 * data artifacts accessible via Hadoop (Local filesystem, Amazon S3, etc.),
 * evaluate clinical queries and write out the results.
 */
public class SparkCqlEvaluator implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(SparkCqlEvaluator.class);
    private static final long serialVersionUID = 1L;

    protected SparkCqlEvaluatorArgs args;

    protected SparkTypeConverter typeConverter;
    
    protected SparkOutputColumnEncoder sparkOutputColumnEncoder;

    /**
     * Store a single copy of the job specification data per thread. This allows the data
     * to be read once from underlying storage and reused across each context
     * evaluation (of which there are many).
     */
    protected static ThreadLocal<CqlEvaluationRequests> jobSpecification = new ThreadLocal<>();
    
    /**
     * Store a single configured copy of the library provider per thread. This allows the
     * library provider to be reused which impacts the ability of the CQL context objects to
     * be reused. CQL context object reuse is important for performance reasons. Context
     * objects are inherently slow to initialize. It is important that this and the 
     * terminologyProvider instances remain the same from one run to the next or unnecessary
     * Context initializations will occur.
     */
    protected static ThreadLocal<CqlLibraryProvider> libraryProvider = new ThreadLocal<>();
    
    /**
     * Store a single configured copy of the terminology provider per thread. See the 
     * discussion in the libraryProvider documentation for complete reasoning.
     */
    protected static ThreadLocal<CqlTerminologyProvider> terminologyProvider = new ThreadLocal<>();
    
    protected Map<String, StructType> calculateSparkSchema(List<String> contextNames, ContextDefinitions contextDefinitions) throws Exception{
        CqlLibraryProvider libProvider = SparkCqlEvaluator.libraryProvider.get();
        if (libProvider == null) {
            libProvider = createLibraryProvider();
            SparkCqlEvaluator.libraryProvider.set(libProvider);
        }


        CqlEvaluationRequests cqlEvaluationRequests = jobSpecification.get();
        if (cqlEvaluationRequests == null) {
            cqlEvaluationRequests = readJobSpecification(args.jobSpecPath);
            jobSpecification.set(cqlEvaluationRequests);
        }

        SparkSchemaCreator sparkSchemaCreator = new SparkSchemaCreator(libProvider, cqlEvaluationRequests, contextDefinitions, sparkOutputColumnEncoder);
        return sparkSchemaCreator.calculateSchemasForContexts(contextNames);
    }

    public SparkCqlEvaluator(SparkCqlEvaluatorArgs args) {
        this.args = args;
    }
    
    public void run(PrintStream out) throws Exception {

        SparkSession.Builder sparkBuilder = SparkSession.builder();
        try (SparkSession spark = sparkBuilder.getOrCreate()) {
            boolean useJava8API = Boolean.valueOf(spark.conf().get("spark.sql.datetime.java8API.enabled"));
            this.typeConverter = new SparkTypeConverter(useJava8API);
            this.sparkOutputColumnEncoder = new DefaultSparkOutputColumnEncoder(args.defaultOutputColumnDelimiter);

            ContextDefinitions contexts = readContextDefinitions(args.contextDefinitionPath);

            Map<String, StructType> resultSchemas = calculateSparkSchema(
                    contexts.getContextDefinitions().stream().map(ContextDefinition::getName).collect(Collectors.toList()),
                    contexts
            );

            List<ContextDefinition> filteredContexts = contexts.getContextDefinitions();
            if (args.aggregations != null && args.aggregations.size() > 0) {
                filteredContexts = filteredContexts.stream().filter(def -> args.aggregations.contains(def.getName()))
                        .collect(Collectors.toList());
            }
            if (filteredContexts.isEmpty()) {
                throw new IllegalArgumentException(
                        "At least one context definition is required (after filtering if enabled).");
            }

            final LongAccumulator contextAccum = spark.sparkContext().longAccumulator("Context");
            final LongAccumulator perContextAccum = spark.sparkContext().longAccumulator("PerContext");
            CustomMetricSparkPlugin.contextAccumGauge.setAccumulator(contextAccum);
            CustomMetricSparkPlugin.perContextAccumGauge.setAccumulator(perContextAccum);
            CustomMetricSparkPlugin.totalContextsToProcessCounter.inc(filteredContexts.size());
            CustomMetricSparkPlugin.currentlyEvaluatingContextGauge.setValue(0);
            
            DatasetRetriever datasetRetriever = new DefaultDatasetRetriever(spark, args.inputFormat);
            ContextRetriever contextRetriever = new ContextRetriever(args.inputPaths, datasetRetriever);
            for (ContextDefinition context : filteredContexts) {
                final String contextName = context.getName();

                StructType resultsSchema = resultSchemas.get(contextName);
                
                if (resultsSchema == null || resultsSchema.fields().length == 0) {
                    LOG.warn("Context " + contextName + " has no defines configured. Skipping.");
                }
                else {
                    LOG.info("Evaluating context " + contextName);

                    final String outputPath = MapUtils.getRequiredKey(args.outputPaths, context.getName(), "outputPath");

                    JavaPairRDD<Object, List<Row>> rowsByContextId = contextRetriever.retrieveContext(context);

                    CustomMetricSparkPlugin.currentlyEvaluatingContextGauge.setValue(CustomMetricSparkPlugin.currentlyEvaluatingContextGauge.getValue() + 1);
                    JavaPairRDD<Object, Map<String, Object>> resultsByContext = rowsByContextId
                            .mapToPair(x -> evaluate(contextName, x, perContextAccum));
                    
                    writeResults(spark, resultsSchema, resultsByContext, outputPath);

                    LOG.info(String.format("Wrote results for context %s to %s", contextName, outputPath));

                    contextAccum.add(1);
                    perContextAccum.reset();
                }
            }
            CustomMetricSparkPlugin.currentlyEvaluatingContextGauge.setValue(0);
            try {
	            Boolean metricsEnabledStr = Boolean.valueOf(spark.conf().get("spark.ui.prometheus.enabled"));
	            if(metricsEnabledStr) {
	            	LOG.info("Prometheus metrics enabled, sleeping for 5.5 seconds to finish gathering metrics");
		            //sleep for just over 5 seconds because Prometheus only polls
		            //every 5 seconds. If spark finishes and goes away immediately after completing,
		            //Prometheus will never be able to poll for the final set of metrics for the spark-submit
		            //The default promtheus config map was changed from 2 minute scrape interval to 5 seconds for spark pods
		            Thread.sleep(5500);
	            }else {
	            	LOG.info("Prometheus metrics not enabled");
	            }
            } catch (NoSuchElementException e) {
            	LOG.info("spark.ui.prometheus.enabled is not set");
            }
        }
    }

    /**
     * Deserialize ContextDefinitions from JSON file.
     * 
     * @param path Path to the JSON file.
     * @return Deserialzied object
     * @throws Exception if any deserialization error occurs
     */
    protected ContextDefinitions readContextDefinitions(String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ContextDefinitions contexts = mapper.readValue(new File(path), ContextDefinitions.class);
        return contexts;
    }

    /**
     * Evaluate the input CQL for a single context + data pair.
     *
     * @param contextName     Context name corresponding to the library context key
     *                        currently under evaluation.
     * @param rowsByContext   Data for a single evaluation context
     * @param perContextAccum Spark accumulator that tracks each individual context
     *                        evaluation
     * @return Evaluation results for all expressions evaluated keyed by the context
     *         ID. Expression names are automatically namespaced according to the
     *         library name to avoid issues arising for expression names matching
     *         between libraries (e.g. LibraryName.ExpressionName).
     * @throws Exception if the model info or CQL libraries cannot be loaded for any
     *                   reason
     */
    protected Tuple2<Object, Map<String, Object>> evaluate(String contextName, Tuple2<Object, List<Row>> rowsByContext,
            LongAccumulator perContextAccum) throws Exception {
        CqlLibraryProvider provider = libraryProvider.get();
        if (provider == null) {
            provider = createLibraryProvider();
            libraryProvider.set(provider);
        }
        
        CqlTerminologyProvider termProvider = terminologyProvider.get();
        if( termProvider == null ) {
            termProvider = createTerminologyProvider();
            terminologyProvider.set(termProvider);
        }
        
        return evaluate(provider, termProvider, contextName, rowsByContext, perContextAccum);
    }


    /**
     * Evaluate the input CQL for a single context + data pair.
     * 
     * @param libraryProvider Library provider providing CQL/ELM content
     * @param termProvider    Terminology provider providing terminology resources
     * @param contextName     Context name corresponding to the library context key
     *                        currently under evaluation.
     * @param rowsByContext   Data for a single evaluation context
     * @param perContextAccum Spark accumulator that tracks each individual context
     *                        evaluation
     * @return Evaluation results for all expressions evaluated keyed by the context
     *         ID. Expression names are automatically namespaced according to the
     *         library name to avoid issues arising for expression names matching
     *         between libraries (e.g. LibraryName.ExpressionName).
     * @throws Exception on general failure including CQL library loading issues
     */
    protected Tuple2<Object, Map<String, Object>> evaluate(CqlLibraryProvider libraryProvider,
            CqlTerminologyProvider termProvider, String contextName, Tuple2<Object, List<Row>> rowsByContext,
            LongAccumulator perContextAccum) throws Exception {

        // Convert the Spark objects to the cohort Java model
        List<DataRow> datarows = rowsByContext._2().stream().map(getDataRowFactory()).collect(Collectors.toList());

        Map<String, List<Object>> dataByDataType = new HashMap<>();
        for (DataRow datarow : datarows) {
            String dataType = (String) datarow.getValue(ContextRetriever.SOURCE_FACT_IDX);
            List<Object> mappedRows = dataByDataType.computeIfAbsent(dataType, x -> new ArrayList<>());
            mappedRows.add(datarow);
        }

        DataRowRetrieveProvider retrieveProvider = new DataRowRetrieveProvider(dataByDataType, termProvider);
        CqlDataProvider dataProvider = new DataRowDataProvider(getDataRowClass(), retrieveProvider);

        CqlEvaluator evaluator = new CqlEvaluator().setLibraryProvider(libraryProvider).setDataProvider(dataProvider)
                .setTerminologyProvider(termProvider);

        CqlEvaluationRequests requests = jobSpecification.get();
        if (requests == null) {
            requests = readJobSpecification(args.jobSpecPath);
            jobSpecification.set(requests);
        }

        return evaluate(rowsByContext, contextName, evaluator, requests, perContextAccum);
    }

    /**
     * Evaluate the input CQL for a single context + data pair.
     *
     * @param contextName     Name of the context used to select measure evaluations.
     *                        
     * @param requests        CqlEvaluationRequests containing lists of libraries,
     *                        expressions, and parameters to evaluate
     * @return Filtered list of CqlEvaluationResult. 
     */
    protected List<CqlEvaluationRequest> filterRequests(CqlEvaluationRequests requests, String contextName) {
        List<CqlEvaluationRequest> requestsForContext = new ArrayList<>();
        
        List<CqlEvaluationRequest> evaluations = requests.getEvaluations();
        if (evaluations != null) {
            requestsForContext = requests.getEvaluationsForContext(contextName);
        }

        if (args.libraries != null && args.libraries.size() > 0) {
            requestsForContext = requestsForContext.stream()
                    .filter(r -> args.libraries.keySet().contains(r.getDescriptor().getLibraryId()))
                    .collect(Collectors.toList());
        }
        
        return requestsForContext;
    }

    /**
     * Evaluate the input CQL for a single context + data pair.
     * 
     * @param rowsByContext   In-memory data for all datatypes related to a single
     *                        context
     * @param contextName     Name of the context used to select measure evaluations.
     * @param evaluator       configured CQLEvaluator (data provider, term provider,
     *                        library provider all previously setup)
     * @param requests        CqlEvaluationRequests containing lists of libraries,
     *                        expressions, and parameters to evaluate
     * @param perContextAccum Spark accumulator that tracks each individual context
     *                        evaluation
     * @return Evaluation results for all expressions evaluated keyed by the context
     *         ID. Expression names are automatically namespaced according to the
     *         library name to avoid issues arising for expression names matching
     *         between libraries (e.g. LibraryName.ExpressionName).
     */
    protected Tuple2<Object, Map<String, Object>> evaluate(Tuple2<Object, List<Row>> rowsByContext,
            String contextName, CqlEvaluator evaluator, CqlEvaluationRequests requests, LongAccumulator perContextAccum) {
        perContextAccum.add(1);
        
        List<CqlEvaluationRequest> requestsForContext = filterRequests(requests, contextName);
        
        Map<String, Object> expressionResults = new HashMap<>();
        for (CqlEvaluationRequest request : requestsForContext) {
            if (args.expressions != null && args.expressions.size() > 0) {
                request.setExpressions(args.expressions);
            }

            // add any global parameters that have not been overridden locally
            if (requests.getGlobalParameters() != null) {
                for (Map.Entry<String, Parameter> globalParameter : requests.getGlobalParameters().entrySet()) {
                    request.getParameters().putIfAbsent(globalParameter.getKey(), globalParameter.getValue());
                }
            }

            try {
                CqlEvaluationResult result = evaluator.evaluate(request, args.debug ? CqlDebug.DEBUG : CqlDebug.NONE);
                for (Map.Entry<String, Object> entry : result.getExpressionResults().entrySet()) {
                    String outputColumnKey = sparkOutputColumnEncoder.getColumnName(request.getDescriptor().getLibraryId(), entry.getKey());
                    expressionResults.put(outputColumnKey, typeConverter.toSparkType(entry.getValue()));
                }
            } catch( Throwable th ) {
                Object contextValue = rowsByContext._1();
                throw new RuntimeException( String.format("CQL evaluation failed for ContextKey: %s, ContextValue: %s, Request: %s", request.getContextKey(), String.valueOf(contextValue), request.toString()), th );
            }
        }

        return new Tuple2<>(rowsByContext._1(), expressionResults);
    }
    
    /**
     * Initialize a library provider that will load resources from the configured path
     * in local storage or from the well-known classpath locations. The library provider
     * comes configured with CQL translation enabled and will use custom modelinfo
     * definitions if provided in the configuration.
     * 
     * @return configured library provider
     * @throws IOException when model info cannot be read
     * @throws FileNotFoundException when a specified model info file cannot be found
     */
    protected CqlLibraryProvider createLibraryProvider() throws IOException, FileNotFoundException {
        
        CqlLibraryProvider fsBasedLp = new DirectoryBasedCqlLibraryProvider(new File(args.cqlPath));
        CqlLibraryProvider cpBasedLp = new ClasspathCqlLibraryProvider("org.hl7.fhir");
        CqlLibraryProvider priorityLp = new PriorityCqlLibraryProvider( fsBasedLp, cpBasedLp );

        // TODO - replace with cohort shared translation component
        final CqlToElmTranslator translator = new CqlToElmTranslator();
        if (args.modelInfoPaths != null && args.modelInfoPaths.size() > 0) {
            for (String path : args.modelInfoPaths) {
                try (Reader r = new FileReader(path)) {
                    translator.registerModelInfo(r);
                }
            }
        }
        
        return new TranslatingCqlLibraryProvider(priorityLp, translator);
    }
    
    /**
     * Initialize a terminology provider.
     * 
     * @return configured terminology provider.
     */
    protected CqlTerminologyProvider createTerminologyProvider() {
        return new UnsupportedTerminologyProvider();
    }

    /**
     * Deserialize CQL Job requests.
     * 
     * @param path Path to CQL jobs file in JSON format
     * @return deserialized jobs object
     * @throws Exception when deserialization fails for any reason
     */
    protected CqlEvaluationRequests readJobSpecification(String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CqlEvaluationRequests requests = mapper.readValue(new File(path), CqlEvaluationRequests.class);
        
        try( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) { 
            Validator validator = factory.getValidator();
            
            Set<ConstraintViolation<CqlEvaluationRequests>> violations = validator.validate( requests );
            if( ! violations.isEmpty() ) {
                StringBuffer sb = new StringBuffer();
                for( ConstraintViolation<CqlEvaluationRequests> violation : violations ) { 
                    sb.append(System.lineSeparator())
                        .append(violation.getPropertyPath().toString())
                        .append(": ")
                        .append(violation.getMessage());
                }
                throw new IllegalArgumentException("Invalid Job Specification: " + sb.toString());
            }
        }
        
        return requests;
    }

    /**
     * Write the results of CQL evaluation to a given storage location.
     *
     * @param spark            Active Spark session
     * @param schema           Schema of the output rows being output.
     * @param resultsByContext CQL evaluation results mapped from context value to a
     *                         map of define to define result.
     * @param outputURI        URI pointing at the location where output data should
     *                         be written.
     */
    protected void writeResults(SparkSession spark, StructType schema, JavaPairRDD<Object, Map<String, Object>> resultsByContext,
            String outputURI) {
        Dataset<Row> dataFrame = spark.createDataFrame(
                resultsByContext
                        .map(t -> {
                            Object contextKey = t._1();
                            Map<String, Object> results = t._2();

                            Object[] data = new Object[schema.fields().length];
                            data[0] = contextKey;
                            for (int i = 1; i < schema.fieldNames().length; i++) {
                                data[i] = results.get(schema.fieldNames()[i]);
                            }
                            return data;
                        })
                        .map(RowFactory::create),
                schema
        );

		if (args.outputPartitions != null) {
			dataFrame = dataFrame.repartition(args.outputPartitions);
		}

        dataFrame.write()
                .mode(args.overwriteResults ? SaveMode.Overwrite : SaveMode.ErrorIfExists)
                .format(args.outputFormat != null ? args.outputFormat : spark.conf().get("spark.sql.sources.default"))
                .save(outputURI);
    }

    /**
     * Get the class object that will represent the individual data rows that will
     * be created and used in the CQL runtime. This is important as the CQL engine
     * uses the package of model classes to map to data provider implementations.
     * This method is provided to allow subclasses to override the data row
     * implementation as needed.
     * 
     * @return data row implementation class
     */
    protected Class<? extends DataRow> getDataRowClass() {
        return SparkDataRow.class;
    }

    /**
     * Get a function that will produce the data row classes described by the
     * getDataRowClass method. This allows subclasses to override data row creation
     * as needed.
     * 
     * @return data row factory function
     */
    protected Function<Row, DataRow> getDataRowFactory() {
        return (row) -> new SparkDataRow(getSparkTypeConverter(), row);
    }

    /**
     * Get the SparkTypeConverter implementation that will be used to do Spark to
     * CQL and CQL to Spark type conversions. This method is provided so that
     * subclasses can override the conversion logic as needed.
     * 
     * @return spark type converter
     */
    protected SparkTypeConverter getSparkTypeConverter() {
        return this.typeConverter;
    }

    public static void main(String[] args) throws Exception {
        SparkCqlEvaluatorArgs programArgs = new SparkCqlEvaluatorArgs();

        JCommander commander = JCommander.newBuilder()
                .programName("SparkCqlEvaluator")
                .addObject(programArgs)
                .build();
        commander.parse(args);

        SparkCqlEvaluator evaluator = new SparkCqlEvaluator(programArgs);
        if (programArgs.help) {
            commander.usage();
        } else {
            evaluator.run(System.out);
        }
    }
}