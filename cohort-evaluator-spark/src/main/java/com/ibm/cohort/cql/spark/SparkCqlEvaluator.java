/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

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
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.fs.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinition;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinitions;
import com.ibm.cohort.cql.spark.aggregation.Join;
import com.ibm.cohort.cql.spark.aggregation.OneToMany;
import com.ibm.cohort.cql.spark.data.SparkDataRow;
import com.ibm.cohort.cql.spark.data.SparkTypeConverter;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import com.ibm.cohort.cql.terminology.UnsupportedTerminologyProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import com.ibm.cohort.datarow.engine.DataRowDataProvider;
import com.ibm.cohort.datarow.engine.DataRowRetrieveProvider;
import com.ibm.cohort.datarow.model.DataRow;

import scala.Tuple2;

/**
 * Given knowledge and clinical data artifacts provided in an Amazon S3
 * compatible storage bucket, evaluate clinical queries defined in the HL7
 * clinical quality language (CQL).
 */
public class SparkCqlEvaluator implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SOURCE_FACT_IDX = "__SOURCE_FACT";

    @Parameter(names = { "-h", "--help" }, description = "Print help text", help = true)
    public boolean help;

    @Parameter(names = { "-d",
            "--context-definitions" }, description = "Filesystem path to the context-definitions file.", required = false)
    public String contextDefinitionPath;

    @DynamicParameter(names = { "-i",
            "--input-path" }, description = "Key-value pair of resource=URI controlling where Spark should read resources referenced in the context definitions file will be read from. Specify multiple files by providing a separate option for each input.", required = true)
    public Map<String, String> inputPaths = new HashMap<>();

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
    Map<String, String> libraries = new HashMap<>();

    @Parameter(names = { "-e",
            "--expression" }, description = "One or more expression names, as defined in the context-definitions file, that should be run in this evaluation. Defaults to all expressions.", required = false)
    Set<String> expressions = new HashSet<>();

    @Parameter(names = { "--debug" }, description = "Enables CQL debug logging")
    public boolean debug = false;

    public SparkTypeConverter typeConverter;

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

            for (ContextDefinition context : filteredContexts) {
                JavaPairRDD<Object, Row> allData = readAllInputData(spark, context, inputPaths);

                JavaPairRDD<Object, List<Row>> rowsByContextId = aggregateByContext(allData, context);

                JavaPairRDD<Object, Map<String, Object>> resultsByContext = rowsByContextId.mapToPair(this::evaluate);

                String outputPath = outputPaths.get(context.getName());
                writeResults(spark, resultsByContext, outputPath);
                out.println(String.format("Wrote results for context %s to %s", context.getName(), outputPath));
            }
        }
    }

    protected ContextDefinitions readContextDefinitions(String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ContextDefinitions contexts = mapper.readValue(new File(path), ContextDefinitions.class);
        return contexts;
    }

    /**
     * Input data is expected to be divided into separate files per datatype. For
     * each datatype to be used in evaluations, read in the data, extract the
     * context ID from whatever column in the data contains the primary/foreign key
     * for the evaluation context, and then create a pair of context to row data.
     * This pair will subsequently be used to reorganize the data by context.
     * 
     * @param spark             Active Spark Session
     * @param contextDefinition ContextDefinition describing the input data to read
     * @param inputPaths        Map of logical resource name to input data path
     * @return pairs of context value to data row for each datatype in the input
     */
    protected JavaPairRDD<Object, Row> readAllInputData(SparkSession spark, ContextDefinition contextDefinition,
            Map<String, String> inputPaths) {
        JavaPairRDD<Object, Row> allData = null;

        String dataType = contextDefinition.getPrimaryDataType();
        String path = getRequiredPath(inputPaths, dataType);

        allData = readDataset(spark, path, dataType, contextDefinition.getPrimaryKeyColumn());

        if (contextDefinition.getRelationships() != null) {
            for (Join join : contextDefinition.getRelationships()) {
                if (join instanceof OneToMany) {
                    OneToMany oneToManyJoin = (OneToMany) join;

                    dataType = oneToManyJoin.getRelatedDataType();
                    path = getRequiredPath(inputPaths, dataType);

                    allData.union(readDataset(spark, path, dataType, oneToManyJoin.getRelatedKeyColumn()));
                } else {
                    throw new UnsupportedOperationException(
                            String.format("No support for join type %s at this time", join.getClass().getSimpleName()));
                }
            }
        }

        return allData;
    }

    protected String getRequiredPath(Map<String, String> paths, String dataType) {
        String path = paths.get(dataType);
        if (path == null) {
            throw new IllegalArgumentException(String.format("No path mapping found for datatype %s", dataType));
        }
        return path;
    }

    /**
     * Read a single datatype's dataset. This assumes that data resides in an Amazon
     * compatible endpoint and is in parquet format.
     * 
     * @param spark         Active Spark Session
     * @param fileURI       The S3 URI pointing at the parquet file
     * @param dataType      The DataType string corresponding to the data being read
     * @param contextColumn The column name in the input data that corresponds to
     *                      the evaluation context
     * @return data mapped from context value to row content
     */
    protected JavaPairRDD<Object, Row> readDataset(SparkSession spark, String fileURI, String dataType,
            String contextColumn) {
        Dataset<Row> dataset = spark.read().load(fileURI).withColumn(SOURCE_FACT_IDX, functions.lit(dataType));

        return dataset.javaRDD().mapToPair(row -> {
            Object joinValue = row.getAs(contextColumn);
            return new Tuple2<>(joinValue, row);
        });
    }

    /**
     * Given a set of rows that are indexed by context value, reorganize the data so
     * that all rows related to the same context are grouped into a single pair.
     * 
     * @param allData           rows mapped from context value to a single data row
     * @param contextDefinition ContextDefinition that contains the relationship
     *                          metadata that describes the needed aggregations
     * @return rows grouped mapped from context value to a list of all data for that
     *         context
     */
    protected JavaPairRDD<Object, List<Row>> aggregateByContext(JavaPairRDD<Object, Row> allData,
            ContextDefinition contextDefinition) {
        // Regroup data by context ID so that all input data for the same
        // context is represented as a single key mapped to a list of rows

        boolean aggregateOnContext = contextDefinition.getRelationships() != null
                && contextDefinition.getRelationships().size() > 0;

        JavaPairRDD<Object, List<Row>> combinedData = null;
        if (aggregateOnContext) {
            combinedData = allData.combineByKey(create -> {
                List<Row> dataRowList = new ArrayList<>();
                dataRowList.add(create);
                return dataRowList;
            }, (list, val) -> {
                list.add(val);
                return list;
            }, (list1, list2) -> {
                List<Row> dataRowList = new ArrayList<>(list1);
                dataRowList.addAll(list2);
                return dataRowList;
            });
        } else {
            // TODO: Not sure how much extra time is spent doing this needless work
            // that only serves to keep the "multirow" and "single row" usecases on the same
            // "java type".
            // If there's a big enough time sink here, then we may want to change
            // `combinedData` to be something super generic.
            combinedData = allData
                    .mapToPair((tuple2) -> new Tuple2<>(tuple2._1(), Collections.singletonList(tuple2._2())));
        }

        return combinedData;
    }

    /**
     * Evaluate the input CQL for a single context + data pair.
     *
     * @param rowsByContext Data for a single evaluation context
     * @return result of the evaluation of each specified expression mapped by
     *         context ID
     * @throws Exception if the model info or CQL libraries cannot be loaded for any
     *                   reason
     */
    protected Tuple2<Object, Map<String, Object>> evaluate(Tuple2<Object, List<Row>> rowsByContext) throws Exception {
        CqlLibraryProvider libraryProvider = new DirectoryBasedCqlLibraryProvider(new File(cqlPath));

        // TODO - replace with cohort shared translation component
        final CqlToElmTranslator translator = new CqlToElmTranslator();
        if (modelInfoPaths != null && modelInfoPaths.size() > 0) {
            for (String path : modelInfoPaths) {
                try (Reader r = new FileReader(path)) {
                    translator.registerModelInfo(r);
                }
            }
        }
        TranslatingCqlLibraryProvider translatingLibraryProvider = new TranslatingCqlLibraryProvider(libraryProvider,
                translator);

        return evaluate(translatingLibraryProvider, rowsByContext);
    }

    /**
     * Evaluate the input CQL for a single context + data pair.
     * 
     * @param libraryProvider Library provider providing CQL/ELM content
     * @param rowsByContext   Data for a single evaluation context
     * @return result of the evaluation of each specified expression mapped by
     *         context ID
     * @throws Exception on general failure including CQL library loading issues
     */
    protected Tuple2<Object, Map<String, Object>> evaluate(CqlLibraryProvider libraryProvider,
            Tuple2<Object, List<Row>> rowsByContext) throws Exception {
        CqlTerminologyProvider termProvider = new UnsupportedTerminologyProvider();

        // Convert the Spark objects to the cohort Java model
        List<DataRow> datarows = rowsByContext._2().stream().map(getDataRowFactory()).collect(Collectors.toList());

        Map<String, List<Object>> dataByDataType = new HashMap<>();
        for (DataRow datarow : datarows) {
            String dataType = (String) datarow.getValue(SOURCE_FACT_IDX);
            List<Object> mappedRows = dataByDataType.computeIfAbsent(dataType, x -> new ArrayList<>());
            mappedRows.add(datarow);
        }

        DataRowRetrieveProvider retrieveProvider = new DataRowRetrieveProvider(dataByDataType, termProvider);
        CqlDataProvider dataProvider = new DataRowDataProvider(getDataRowClass(), retrieveProvider);

        CqlEvaluator evaluator = new CqlEvaluator().setLibraryProvider(libraryProvider).setDataProvider(dataProvider)
                .setTerminologyProvider(termProvider);

        CqlEvaluationRequests requests = readJobSpecification(jobSpecPath);

        List<CqlEvaluationRequest> filteredRequests = requests.getEvaluations();
        if (libraries != null && libraries.size() > 0) {
            filteredRequests = filteredRequests.stream()
                    .filter(r -> libraries.keySet().contains(r.getDescriptor().getLibraryId()))
                    .collect(Collectors.toList());
        }
        return evaluate(rowsByContext, evaluator, requests);
    }

    protected Tuple2<Object, Map<String, Object>> evaluate(Tuple2<Object, List<Row>> rowsByContext,
            CqlEvaluator evaluator, CqlEvaluationRequests requests) {
        Map<String, Object> expressionResults = new HashMap<>();
        for (CqlEvaluationRequest request : requests.getEvaluations()) {
            if (expressions != null && expressions.size() > 0) {
                request.setExpressions(expressions);
            }

            // add any global parameters that have not been overridden locally
            if( requests.getGlobalParameters() != null ) {
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

        resultsByContext.saveAsTextFile(outputURI + UUID.randomUUID().toString());
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
