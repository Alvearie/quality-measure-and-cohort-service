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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ibm.cohort.cql.spark.aggregation.ContextRetriever;
import com.ibm.cohort.cql.spark.data.DatasetRetriever;
import com.ibm.cohort.cql.spark.data.ParquetDatasetRetriever;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.LongAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.CqlDebug;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;
import com.ibm.cohort.cql.evaluation.CqlEvaluator;
import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.library.PriorityCqlLibraryProvider;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinition;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinitions;
import com.ibm.cohort.cql.spark.data.SparkDataRow;
import com.ibm.cohort.cql.spark.data.SparkTypeConverter;
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



    @Parameter(names = { "-h", "--help" }, description = "Print help text", help = true)
    public boolean help;

    @Parameter(names = { "-d",
            "--context-definitions" }, description = "Filesystem path to the context-definitions file.", required = false)
    public String contextDefinitionPath;

    @Parameter(names = { "--input-format" }, description = "Spark SQL format identifier for input files. If not provided, the value of spark.sql.datasources.default is used.", required = false)
    public String inputFormat;
    
    @DynamicParameter(names = { "-i",
            "--input-path" }, description = "Key-value pair of resource=URI controlling where Spark should read resources referenced in the context definitions file will be read from. Specify multiple files by providing a separate option for each input.", required = true)
    public Map<String, String> inputPaths = new HashMap<>();

    @Parameter(names = { "--output-format" }, description = "Spark SQL format identifier for output files. If not provided, the value of spark.sql.datasources.default is used.", required = false)
    public String outputFormat;
    
    @DynamicParameter(names = { "-o",
            "--output-path" }, description = "Key-value pair of context=URI controlling where Spark should write the results of CQL evaluation requests. Specify multiple files by providing a separate option for each output.", required = true)
    public Map<String, String> outputPaths = new HashMap<>();

    @Parameter(names = { "-j", "--jobs" }, description = "Filesystem path to the CQL job file", required = true)
    public String jobSpecPath;

    @Parameter(names = { "-m",
            "--model-info" }, description = "Filesystem path(s) to custom model-info files that may be required for CQL translation.", required = true)
    public List<String> modelInfoPaths = new ArrayList<>();

    @Parameter(names = { "-c",
            "--cql-path" }, description = "Filesystem path to the location containing the CQL libraries referenced in the jobs file.", required = true)
    public String cqlPath;

    @Parameter(names = { "-a",
            "--aggregation" }, description = "One or more context names, as defined in the context-definitions file, that should be run in this evaluation. Defaults to all evaluations.", required = false)
    public List<String> aggregations = new ArrayList<>();

    @DynamicParameter(names = { "-l",
            "--library" }, description = "One or more library=version key-value pair(s), as defined in the jobs file, that describe the libraries that should be run in this evaluation. Defaults to all libraries. Specify multiple libraries by providing a separate option for each library.", required = false)
    public Map<String, String> libraries = new HashMap<>();

    @Parameter(names = { "-e",
            "--expression" }, description = "One or more expression names, as defined in the context-definitions file, that should be run in this evaluation. Defaults to all expressions.", required = false)
    public Set<String> expressions = new HashSet<>();

    @Parameter(names = { "-n",
            "--output-partitions" }, description = "Number of partitions to use when storing data", required = false)
    public Integer outputPartitions = null;

    @Parameter(names = { "--debug" }, description = "Enables CQL debug logging")
    public boolean debug = false;

    protected SparkTypeConverter typeConverter;

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

    public void run(PrintStream out) throws Exception {

        SparkSession.Builder sparkBuilder = SparkSession.builder();

        try (SparkSession spark = sparkBuilder.getOrCreate()) {
            boolean useJava8API = Boolean.valueOf(spark.conf().get("spark.sql.datetime.java8API.enabled"));
            this.typeConverter = new SparkTypeConverter(useJava8API);

            ContextDefinitions contexts = readContextDefinitions(contextDefinitionPath);

            List<ContextDefinition> filteredContexts = contexts.getContextDefinitions();
            if (aggregations != null && aggregations.size() > 0) {
                filteredContexts = filteredContexts.stream().filter(def -> aggregations.contains(def.getName()))
                        .collect(Collectors.toList());
            }
            if (filteredContexts.isEmpty()) {
                throw new IllegalArgumentException(
                        "At least one context definition is required (after filtering if enabled).");
            }

            final LongAccumulator contextAccum = spark.sparkContext().longAccumulator("Context");
            final LongAccumulator perContextAccum = spark.sparkContext().longAccumulator("PerContext");
            DatasetRetriever datasetRetriever = new ParquetDatasetRetriever(spark);
            ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);
            for (ContextDefinition context : filteredContexts) {
                final String contextName = context.getName();
                LOG.info("Evaluating context " + contextName);

                final String outputPath = MapUtils.getRequiredKey(outputPaths, context.getName(), "outputPath");

                JavaPairRDD<Object, List<Row>> rowsByContextId = contextRetriever.retrieveContext(context);

                JavaPairRDD<Object, Map<String, Object>> resultsByContext = rowsByContextId
                        .mapToPair(x -> evaluate(contextName, x, perContextAccum));

                writeResults(spark, resultsByContext, outputPath);
                LOG.info(String.format("Wrote results for context %s to %s", contextName, outputPath));

                contextAccum.add(1);
                perContextAccum.reset();
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
            requests = readJobSpecification(jobSpecPath);
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
            requestsForContext = requests.getEvaluations().stream()
                    .filter(r -> r.getContextKey().equals(contextName)).collect(Collectors.toList());
        }

        if (libraries != null && libraries.size() > 0) {
            requestsForContext = requestsForContext.stream()
                    .filter(r -> libraries.keySet().contains(r.getDescriptor().getLibraryId()))
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
            if (expressions != null && expressions.size() > 0) {
                request.setExpressions(expressions);
            }

            // add any global parameters that have not been overridden locally
            if (requests.getGlobalParameters() != null) {
                for (Map.Entry<String, Object> globalParameter : requests.getGlobalParameters().entrySet()) {
                    request.getParameters().putIfAbsent(globalParameter.getKey(), globalParameter.getValue());
                }
            }

            CqlEvaluationResult result = evaluator.evaluate(request, debug ? CqlDebug.DEBUG : CqlDebug.NONE);
            for (Map.Entry<String, Object> entry : result.getExpressionResults().entrySet()) {
                String outputColumnKey = request.getDescriptor().getLibraryId() + "." + entry.getKey();
                expressionResults.put(outputColumnKey, typeConverter.toSparkType(entry.getValue()));
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
        
        CqlLibraryProvider fsBasedLp = new DirectoryBasedCqlLibraryProvider(new File(cqlPath));
        CqlLibraryProvider cpBasedLp = new ClasspathCqlLibraryProvider("org.hl7.fhir");
        CqlLibraryProvider priorityLp = new PriorityCqlLibraryProvider( fsBasedLp, cpBasedLp );

        // TODO - replace with cohort shared translation component
        final CqlToElmTranslator translator = new CqlToElmTranslator();
        if (modelInfoPaths != null && modelInfoPaths.size() > 0) {
            for (String path : modelInfoPaths) {
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
        return requests;
    }

    /**
     * Write the results of CQL evaluation to a given storage location.
     *
     * @param spark            Active Spark session
     * @param resultsByContext CQL evaluation results mapped from context value to a
     *                         map of define to define result.
     * @param outputURI        URI pointing at the location where output data should
     *                         be written.
     */
    protected void writeResults(SparkSession spark, JavaPairRDD<Object, Map<String, Object>> resultsByContext,
            String outputURI) {

//        TODO - output the data in delta lake format.
//        Map<String,Object> columnData = resultsByContext.take(1).get(0)._2();
//        StructType schema = new StructType();
//        schema.add(name, DataTypes.cre)
//        
//        JavaRDD<Row> rows = resultsByContext.map( tuple -> {
//            List<Object> columns = 
//            
//            RowFactory.create(tuple._1(), )
//        })
        
        // TODO - use the outputFormat parameter if it isn't null
        

        if (outputPartitions != null) {
            resultsByContext = resultsByContext.repartition(outputPartitions.intValue());
        }
        String uuid = UUID.randomUUID().toString();
        LOG.info("Batch UUID " + uuid);
        resultsByContext.saveAsTextFile(outputURI + uuid);
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
        SparkCqlEvaluator evaluator = new SparkCqlEvaluator();

        JCommander commander = JCommander.newBuilder().addObject(evaluator).build();
        commander.parse(args);

        if (evaluator.help) {
            commander.usage();
        } else {
            evaluator.run(System.out);
        }
    }
}
