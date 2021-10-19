/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.SparkException;
import org.apache.spark.deploy.SparkHadoopUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.delta.DeltaLog;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.util.SerializableConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.evaluation.parameters.DateParameter;
import com.ibm.cohort.cql.evaluation.parameters.DecimalParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntegerParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntervalParameter;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.evaluation.parameters.StringParameter;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinitions;
import com.ibm.cohort.cql.spark.data.Patient;

public class SparkCqlEvaluatorTest extends BaseSparkTest {
    private static final long serialVersionUID = 1L;

    private SparkCqlEvaluatorArgs args;
    private SparkCqlEvaluator evaluator;
    private SparkSession spark;
    
    @Before
    public void setUp() {
        this.args = new SparkCqlEvaluatorArgs();
        this.evaluator = new SparkCqlEvaluator(args);
        this.spark = initializeSession(Java8API.ENABLED);

        SparkCqlEvaluator.jobSpecification.remove();
        SparkCqlEvaluator.libraryProvider.remove();
        SparkCqlEvaluator.terminologyProvider.remove();
        SparkCqlEvaluator.sparkOutputColumnEncoder.remove();
    }

    /**
     * This is the code that was used to generate the test data used in the simple-job 
     * tests.
     */
    @Test
    @Ignore
    public void createPatientTestData() {
        int rowCount = 10;
        int groups = rowCount;
        
        List<Patient> sourceData = new ArrayList<>();
        for(int i=0; i<rowCount; i++) {
            Patient pojo = Patient.randomInstance();
            pojo.setId( String.valueOf(i % groups) );
            sourceData.add( pojo );
        }

        Dataset<Row> dataset = spark.createDataFrame(sourceData, Patient.class);

        Path tempFile = Paths.get("src/test/resources/simple-job/testdata", "patient" );
        dataset.write().format("delta").save(tempFile.toString());
    }
    
    @Test
    public void testReadAggregateSuccess() throws Exception {
        String outputLocation = "target/output/aggregate-success/patient_cohort";
        
        String [] args = new String[] {
          "-d", "src/test/resources/simple-job/context-definitions.json",
          "-j", "src/test/resources/simple-job/cql-jobs.json",
          "-m", "src/test/resources/simple-job/modelinfo/simple-modelinfo-1.0.0.xml",
          "-c", "src/test/resources/simple-job/cql",
          "-i", "Patient=" + new File("src/test/resources/simple-job/testdata/patient").toURI().toString(),
          "-o", "Patient=" + new File(outputLocation).toURI().toString(),
          "--output-format", "delta",
          "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);
        
        validateOutputCountsAndColumns(outputLocation, new HashSet<>(Arrays.asList("id", "SampleLibrary|IsFemale")), 10, "delta");
    }

    @Test
    public void testAllTypesEvaluationSuccess() throws Exception {
        File inputDir = new File("src/test/resources/alltypes/");
        File outputDir = new File("target/output/alltypes/");

        File patientFile = new File(outputDir, "Patient_cohort");
        File aFile = new File(outputDir, "A_cohort");
        File bFile = new File(outputDir, "B_cohort");
        File cFile = new File(outputDir, "C_cohort");
        File dFile = new File(outputDir, "D_cohort");

        String [] args = new String[] {
          "-d", "src/test/resources/alltypes/metadata/context-definitions.json",
          "-j", "src/test/resources/alltypes/metadata/cql-jobs.json",
          "-m", "src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml",
          "-c", "src/test/resources/alltypes/cql",
          "--input-format", "parquet",
          "-i", "A=" + new File(inputDir, "testdata/test-A.parquet").toURI().toString(),
          "-i", "B=" + new File(inputDir, "testdata/test-B.parquet").toURI().toString(),
          "-i", "C=" + new File(inputDir, "testdata/test-C.parquet").toURI().toString(),
          "-i", "D=" + new File(inputDir, "testdata/test-D.parquet").toURI().toString(),
          "-o", "Patient=" + patientFile.toURI().toString(),
          "-o", "A=" + aFile.toURI().toString(),
          "-o", "B=" + bFile.toURI().toString(),
          "-o", "C=" + cFile.toURI().toString(),
          "-o", "D=" + dFile.toURI().toString(),
          "-n", "10",
          "--output-format", "parquet",
          "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);

        // Expected rows per context were derived from inspecting the input data
        // by hand and counting the unique values for each context's key column.
        validateOutputCountsAndColumns(patientFile.toURI().toString(), new HashSet<>(Arrays.asList("pat_id", "MeasureAB|cohort")), 100, "parquet");
        validateOutputCountsAndColumns(aFile.toURI().toString(), new HashSet<>(Arrays.asList("id_col", "MeasureA|cohort")), 572, "parquet");
        validateOutputCountsAndColumns(bFile.toURI().toString(), new HashSet<>(Arrays.asList("id", "MeasureB|cohort")), 575, "parquet");
        validateOutputCountsAndColumns(cFile.toURI().toString(), new HashSet<>(Arrays.asList("id", "MeasureC|cohort")), 600, "parquet");
        validateOutputCountsAndColumns(dFile.toURI().toString(), new HashSet<>(Arrays.asList("id", "MeasureD|cohort")), 567, "parquet");
    }

    @Test
    public void testAnyColumnEvaluation() throws Exception {
        File inputDir = new File("src/test/resources/alltypes/");
        File outputDir = new File("target/output/any-column/");

        File aFile = new File(outputDir, "A_cohort");

        String [] args = new String[] {
            "-d", "src/test/resources/any-column/metadata/context-definitions.json",
            "-j", "src/test/resources/any-column/metadata/cql-jobs.json",
            "-m", "src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml",
            "-c", "src/test/resources/any-column/cql",
            "--input-format", "parquet",
            "-i", "A=" + new File(inputDir, "testdata/test-A.parquet").toURI(),
            "-o", "A=" + aFile.toURI(),
            "-n", "10",
            "--output-format", "parquet",
            "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);

        validateOutputCountsAndColumns(aFile.toURI().toString(), new HashSet<>(Arrays.asList("id_col", "MeasureAnyColumn|cohort")), 572, "parquet");

        StructType outputSchema = new StructType()
            .add("id_col", DataTypes.StringType, false)
            .add("MeasureAnyColumn|cohort", DataTypes.BooleanType, true);
        List<Row> expectedRows = jsonToRows("src/test/resources/any-column/output/expected.json", outputSchema);

        validateOutput(
            aFile.toURI().toString(),
            expectedRows,
            outputSchema,
            "parquet"
        );
    }

    private List<Row> jsonToRows(String jsonPath, StructType schema) {
        spark = initializeSession(Java8API.ENABLED);
        Dataset<Row> ds = spark.read().schema(schema).json(jsonPath);

        return ds.collectAsList();
    }

    private void validateOutputCountsAndColumns(String filename, Set<String> columnNames, int numExpectedRows, String expectedFormat) {
        // SparkCqlEvaluator closes the SparkSession. Make sure we have one opened before any validation.
        DeltaLog.clearCache();
        spark = initializeSession(Java8API.ENABLED);
        Dataset<Row> results = spark.read().format(expectedFormat).load(filename);
        validateColumnNames(results.schema(), columnNames);

        assertEquals(numExpectedRows, results.count());
    }
    

    private void validateColumnNames(StructType schema, Set<String> columnNames) {
        for (String field : schema.fieldNames()) {
            assertTrue(columnNames.contains(field));
        }
        assertEquals(columnNames.size(), schema.fields().length);
    }
    
    @Test
    public void testValidateOutput() throws Exception {
        File inputDir = new File("src/test/resources/output-validation/");
        File outputDir = new File("target/output/output-validation/");

        File context1IdFile = new File(outputDir, "context-1-id");
        File context2IdFile = new File(outputDir, "context-2-id");
        File patientFile = new File(outputDir, "patient-context");

        String [] args = new String[] {
                "-d", "src/test/resources/output-validation/metadata/context-definitions.json",
                "-j", "src/test/resources/output-validation/metadata/cql-jobs.json",
                "-m", "src/test/resources/output-validation/modelinfo/simple-all-types-model-info.xml",
                "-c", "src/test/resources/output-validation/cql",
                "--input-format", "parquet",
                "-i", "Type1=" + new File(inputDir, "testdata/Type1").toURI().toString(),
                "-i", "Type2=" + new File(inputDir, "testdata/Type2").toURI().toString(),
                "-i", "Patient=" + new File(inputDir, "testdata/Patient").toURI().toString(),
                "-o", "Context1Id=" + context1IdFile.toURI().toString(),
                "-o", "Context2Id=" + context2IdFile.toURI().toString(),
                "-o", "Patient=" + patientFile.toURI().toString(),
                "--output-format", "delta",
                "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);
        
        
        validateOutput(
                context1IdFile.toURI().toString(),
                Arrays.asList(
                        RowFactory.create(0, null, null, null, false),
                        RowFactory.create(1, null, null, null, false),
                        RowFactory.create(2, 33, "string1", new BigDecimal(9.989), true),
                        RowFactory.create(3, 22, "string2", new BigDecimal(-2.816), true),
                        RowFactory.create(4, 22, "string1", new BigDecimal(-4.926), true)
                ),
                new StructType()
                        .add("id", DataTypes.IntegerType, false)
                        .add("Context1Id|define_integer", DataTypes.IntegerType, true)
                        .add("Context1Id|define_string", DataTypes.StringType, true)
                        // Decimal precision currently hardcoded in QNameToDataTypeConverter
                        .add("Context1Id|define_decimal", DataTypes.createDecimalType(28, 8), true)
                        .add("Context1Id|define_boolean", DataTypes.BooleanType, true),
                "delta"
        );

        validateOutput(
                context2IdFile.toURI().toString(),
                Arrays.asList(
                        // Expected Instants derived from reading the input files and printing out
                        // the datetime column using UTC
                        RowFactory.create(0, LocalDate.of(2002, 9, 27), Instant.parse("2002-05-31T13:19:08.512Z")),
                        RowFactory.create(1, LocalDate.of(2002, 3, 10), Instant.parse("2002-05-07T09:19:19.31Z")),
                        RowFactory.create(2, LocalDate.of(2002, 5, 29), Instant.parse("2002-03-13T13:00:52.859Z")),
                        RowFactory.create(3, null, null),
                        RowFactory.create(4, LocalDate.of(2002, 5, 21), Instant.parse("2002-02-17T08:59:43.793Z"))
                ),
                new StructType()
                        .add("id", DataTypes.IntegerType, false)
                        .add("Context2Id|define_date", DataTypes.DateType, true)
                        .add("Context2Id|define_datetime", DataTypes.TimestampType, true),
                "delta"
        );

        validateOutput(
                patientFile.toURI().toString(),
                Arrays.asList(
                        // Expected Instants derived from reading the input files and printing out
                        // the datetime column using UTC
                        RowFactory.create(0, false),
                        RowFactory.create(1, false),
                        RowFactory.create(2, true),
                        RowFactory.create(3, false),
                        RowFactory.create(4, true)
                ),
                new StructType()
                        .add("id", DataTypes.IntegerType, false)
                        .add("PatientMeasure|cohort", DataTypes.BooleanType, true),
                "delta"
        );
    }

    @Test
    public void testValidateOutputOptionEAndOptionL() throws Exception {
        File inputDir = new File("src/test/resources/output-validation/");
        File outputDir = new File("target/output/output-validation-option-el/");

        File context1IdFile = new File(outputDir, "context-1-id");

        String[] args = new String[]{
                "-d", "src/test/resources/output-validation/metadata/context-definitions.json",
                "-j", "src/test/resources/output-validation/metadata/cql-jobs-combined.json",
                "-m", "src/test/resources/output-validation/modelinfo/simple-all-types-model-info.xml",
                "-c", "src/test/resources/output-validation/cql",
                "--input-format", "parquet",
                "-i", "Type1=" + new File(inputDir, "testdata/Type1").toURI().toString(),
                "-e", "define_integer",
                "-l", "Context1Id=1.0.0",
                "-o", "Context1Id=" + context1IdFile.toURI().toString(),
                "--output-format", "parquet",
                "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);


        validateOutput(
                context1IdFile.toURI().toString(),
                Arrays.asList(
                        RowFactory.create(0, null),
                        RowFactory.create(1, null),
                        RowFactory.create(2, 33),
                        RowFactory.create(3, 22),
                        RowFactory.create(4, 22)
                ),
                new StructType()
                        .add("id", DataTypes.IntegerType, false)
                        .add("Context1Id|define_integer", DataTypes.IntegerType, true),
                "parquet"
        );
    }

    @Test
    public void testValidateOutputOptionEAndOptionA() throws Exception {
        File inputDir = new File("src/test/resources/output-validation/");
        File outputDir = new File("target/output/output-validation-option-ea/");

        File context1IdFile = new File(outputDir, "context-1-id");

        String[] args = new String[]{
                "-d", "src/test/resources/output-validation/metadata/context-definitions.json",
                "-j", "src/test/resources/output-validation/metadata/cql-jobs-combined.json",
                "-m", "src/test/resources/output-validation/modelinfo/simple-all-types-model-info.xml",
                "-c", "src/test/resources/output-validation/cql",
                "--input-format", "parquet",
                "-i", "Type1=" + new File(inputDir, "testdata/Type1").toURI().toString(),
                "-e", "define_integer",
                "-a", "Context1Id",
                "-o", "Context1Id=" + context1IdFile.toURI().toString(),
                "--output-format", "parquet",
                "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);


        validateOutput(
                context1IdFile.toURI().toString(),
                Arrays.asList(
                        RowFactory.create(0, null),
                        RowFactory.create(1, null),
                        RowFactory.create(2, 33),
                        RowFactory.create(3, 22),
                        RowFactory.create(4, 22)
                ),
                new StructType()
                        .add("id", DataTypes.IntegerType, false)
                        .add("Context1Id|define_integer", DataTypes.IntegerType, true),
                "parquet"
        );
    }

    @Test
    public void testValidateSameDefinesWithOutputMapping() throws Exception {
        File inputDir = new File("src/test/resources/column-mapping-validation/");
        File outputDir = new File("target/output/column-mapping-validation/");

        File patientFile = new File(outputDir, "patient");

        String[] args = new String[]{
                "-d", "src/test/resources/column-mapping-validation/metadata/context-definitions.json",
                "-j", "src/test/resources/column-mapping-validation/metadata/cql-jobs.json",
                "-m", "src/test/resources/column-mapping-validation/modelinfo/simple-all-types-model-info.xml",
                "-c", "src/test/resources/column-mapping-validation/cql",
                "--input-format", "parquet",
                "-i", "Patient=" + new File(inputDir, "testdata/Patient").toURI().toString(),
                "-o", "Patient=" + patientFile.toURI().toString(),
                "--output-format", "parquet",
                "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);


        validateOutput(
                patientFile.toURI().toString(),
                Arrays.asList(
                        RowFactory.create(0, 5, 10, 1),
                        RowFactory.create(1, 5, 10, 1),
                        RowFactory.create(2, 5, 10, 1),
                        RowFactory.create(3, 5, 10, 1),
                        RowFactory.create(4, 5, 10, 1)
                ),
                new StructType()
                        .add("id", DataTypes.IntegerType, false)
                        .add("all5", DataTypes.IntegerType, true)
                        .add("all10", DataTypes.IntegerType, true)
                        .add("ParameterMeasure|cohort", DataTypes.IntegerType, true),
                "parquet"
        );
    }
    
    private void validateOutput(String filename, List<Row> expectedRows, StructType schema, String expectedFormat) {
        // SparkCqlEvaluator closes the SparkSession. Make sure we have one opened before any validation.
        DeltaLog.clearCache();
        spark = initializeSession(Java8API.ENABLED);
        Dataset<Row> actualDataFrame = spark.read().format(expectedFormat).load(filename);
        // Column names with a dot in them need escaped with backticks
        List<String> columnList = Arrays.asList(actualDataFrame.columns());
        String[] actualColumns = columnList.toArray(new String[columnList.size()]);

        assertEquals(schema.fields().length, actualColumns.length);

        // Make sure columns are in the same order in both dataframes
        Dataset<Row> expectedDataFrame = spark.createDataFrame(expectedRows, schema)
                .select(actualColumns[0], Arrays.copyOfRange(actualColumns, 1, actualColumns.length));

        assertEquals(0, expectedDataFrame.except(actualDataFrame).count());
        assertEquals(0, actualDataFrame.except(expectedDataFrame).count());

    }
    
    @Test
    public void testUnknownLibraryNameThrowsException() throws Exception {
        File inputDir = new File("src/test/resources/alltypes/");
        File outputDir = new File("target/output/alltypes/");

        File aFile = new File(outputDir, "A_cohort");

        String [] args = new String[] {
                "-d", "src/test/resources/alltypes/metadata/context-definitions.json",
                // cql-jobs file references an unknown library in its evaluation request
                "-j", "src/test/resources/alltypes/metadata/unknown-library-cql-jobs.json",
                "-m", "src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml",
                "-c", "src/test/resources/alltypes/cql",
                "--input-format", "parquet",
                "-i", "A=" + new File(inputDir, "testdata/test-A.parquet").toURI().toString(),
                "-o", "A=" + aFile.toURI().toString(),
                "-n", "10",
                "--overwrite-output-for-contexts"
        };

        Exception ex = assertThrows( IllegalArgumentException.class, () -> SparkCqlEvaluator.main(args) );
        assertTrue( "Unexpected exception message", ex.getMessage().contains("Library not found"));
    }

    @Test
    public void testUnknownDefineThrowsException() throws Exception {
        File inputDir = new File("src/test/resources/alltypes/");
        File outputDir = new File("target/output/alltypes/");

        File aFile = new File(outputDir, "A_cohort");

        String [] args = new String[] {
                "-d", "src/test/resources/alltypes/metadata/context-definitions.json",
                // cql-jobs file references an unknown define in its evaluation request
                "-j", "src/test/resources/alltypes/metadata/unknown-define-cql-jobs.json",
                "-m", "src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml",
                "-c", "src/test/resources/alltypes/cql",
                "--input-format", "parquet",
                "-i", "A=" + new File(inputDir, "testdata/test-A.parquet").toURI().toString(),
                "-o", "A=" + aFile.toURI().toString(),
                "-n", "10",
                "--overwrite-output-for-contexts"
        };

        Exception ex = assertThrows( IllegalArgumentException.class, () -> SparkCqlEvaluator.main(args) );
        assertTrue( "Unexpected exception message", ex.getMessage().contains("is configured in the CQL jobs file, but not found in"));
    }
    
    @Test
    public void testListResultThrowsException() throws Exception {
        File inputDir = new File("src/test/resources/alltypes/");
        File outputDir = new File("target/output/alltypes/");

        File patientFile = new File(outputDir, "Patient_cohort");
        File aFile = new File(outputDir, "A_cohort");
        File bFile = new File(outputDir, "B_cohort");
        File cFile = new File(outputDir, "C_cohort");
        File dFile = new File(outputDir, "D_cohort");

        String [] args = new String[] {
          "-d", "src/test/resources/alltypes/metadata/context-definitions.json",
          "-j", "src/test/resources/alltypes/metadata/list-result-cql-jobs.json",
          "-m", "src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml",
          "-c", "src/test/resources/alltypes/cql",
          "--input-format", "parquet",
          "-i", "A=" + new File(inputDir, "testdata/test-A.parquet").toURI().toString(),
          "-i", "B=" + new File(inputDir, "testdata/test-B.parquet").toURI().toString(),
          "-i", "C=" + new File(inputDir, "testdata/test-C.parquet").toURI().toString(),
          "-i", "D=" + new File(inputDir, "testdata/test-D.parquet").toURI().toString(),
          "-o", "Patient=" + patientFile.toURI().toString(),
          "-o", "A=" + aFile.toURI().toString(),
          "-o", "B=" + bFile.toURI().toString(),
          "-o", "C=" + cFile.toURI().toString(),
          "-o", "D=" + dFile.toURI().toString(),
          "-n", "10",
          "--overwrite-output-for-contexts"
        };

        Exception ex = assertThrows( IllegalArgumentException.class, () -> SparkCqlEvaluator.main(args) );
        assertTrue( "Unexpected exception message", ex.getMessage().contains("has a null result type"));
    }
    
    @Test
    public void testCQLEngineThrowsException() throws Exception {
        File inputDir = new File("src/test/resources/alltypes/");
        File outputDir = new File("target/output/alltypes/");

        File patientFile = new File(outputDir, "Patient_cohort");
        File aFile = new File(outputDir, "A_cohort");
        File bFile = new File(outputDir, "B_cohort");
        File cFile = new File(outputDir, "C_cohort");
        File dFile = new File(outputDir, "D_cohort");

        String [] args = new String[] {
          "-d", "src/test/resources/alltypes/metadata/context-definitions.json",
          "-j", "src/test/resources/alltypes/metadata/throws-exception-cql-jobs.json",
          "-m", "src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml",
          "-c", "src/test/resources/alltypes/cql",
          "--input-format", "parquet",
          "-i", "A=" + new File(inputDir, "testdata/test-A.parquet").toURI().toString(),
          "-i", "B=" + new File(inputDir, "testdata/test-B.parquet").toURI().toString(),
          "-i", "C=" + new File(inputDir, "testdata/test-C.parquet").toURI().toString(),
          "-i", "D=" + new File(inputDir, "testdata/test-D.parquet").toURI().toString(),
          "-o", "Patient=" + patientFile.toURI().toString(),
          "-o", "A=" + aFile.toURI().toString(),
          "-o", "B=" + bFile.toURI().toString(),
          "-o", "C=" + cFile.toURI().toString(),
          "-o", "D=" + dFile.toURI().toString(),
          "-n", "10",
          "--overwrite-output-for-contexts"
        };

        Throwable th = assertThrows( SparkException.class, () -> SparkCqlEvaluator.main(args) );
        assertStackTraceContainsMessage(th, "CQL evaluation failed");
    }
    
    private CqlEvaluationRequest makeEvaluationRequest(String contextName, String libraryName, String libraryVersion) {
        CqlEvaluationRequest cqlEvaluationRequest = new CqlEvaluationRequest();
        cqlEvaluationRequest.setContextKey(contextName);
        cqlEvaluationRequest.setContextValue("NA");

        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor();
        descriptor.setLibraryId(libraryName);
        descriptor.setVersion(libraryVersion);
        
        cqlEvaluationRequest.setDescriptor(descriptor);
        
        return cqlEvaluationRequest;
    }

    @Test
    public void testGetFilteredRequestsNoFilters() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();
        requests.setEvaluations(Arrays.asList(
                makeEvaluationRequest("context", "lib1", "1.0.0")
        ));

        CqlEvaluationRequests actual = evaluator.getFilteredRequests(
                requests,
                null,
                null
        );

        assertEquals(1, actual.getEvaluations().size());
    }

    @Test
    public void testGetFilteredRequestsNoEvaluationRequestsForLibraries() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();

        CqlEvaluationRequest request = makeEvaluationRequest("context", "lib1", "1.0.0");
        request.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohort")));


        CqlEvaluationRequest request2 = makeEvaluationRequest("context", "lib2", "1.0.0");
        request2.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohort")));
        
        List<CqlEvaluationRequest> evaluations = Arrays.asList(
                request,
                request2
        );

        requests.setEvaluations(evaluations);

        Map<String, String> libs = new HashMap<String, String>() {{
            put("lib3", "1.0.0");
            put("lib4", "1.0.0");
        }};

        CqlEvaluationRequests actual = evaluator.getFilteredRequests(
                requests,
                libs,
                null
        );

        assertTrue(actual.getEvaluations().isEmpty());
    }

    @Test
    public void testGetFilteredRequestsFilterToLibrariesIgnoresVersion() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();

        CqlEvaluationRequest request = makeEvaluationRequest("context", "lib1", "1.0.0");
        request.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohort")));


        CqlEvaluationRequest request2 = makeEvaluationRequest("context", "lib2", "1.0.0");
        request2.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohort")));

        CqlEvaluationRequest request3 = makeEvaluationRequest("context", "lib3", "1.0.0");
        request.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohort")));


        CqlEvaluationRequest request4 = makeEvaluationRequest("context", "lib4", "1.0.0");
        request2.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohort")));

        List<CqlEvaluationRequest> evaluations = Arrays.asList(
                request,
                request2,
                request3,
                request4
        );

        requests.setEvaluations(evaluations);

        Map<String, String> libs = new HashMap<String, String>() {{
            put("lib3", "7.0.0");
            put("lib4", "1.0.0");
        }};

        CqlEvaluationRequests actual = evaluator.getFilteredRequests(
                requests,
                libs,
                null
        );

        assertEquals(2, actual.getEvaluations().size());
        for (CqlEvaluationRequest cqlEvaluationRequest : actual.getEvaluations()) {
            assertTrue(libs.containsKey(cqlEvaluationRequest.getDescriptor().getLibraryId()));
        }
    }

    @Test
    public void testGetFilteredRequestsOverwriteEvaluations() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();

        CqlEvaluationRequest request = makeEvaluationRequest("context", "lib1", "1.0.0");
        request.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohortOrig")));


        CqlEvaluationRequest request2 = makeEvaluationRequest("context", "lib2", "1.0.0");
        request2.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohortOrig")));

        List<CqlEvaluationRequest> evaluations = Arrays.asList(
                request,
                request2
        );

        requests.setEvaluations(evaluations);

        Map<String, String> libs = new HashMap<String, String>() {{
            put("lib1", "1.0.0");
            put("lib2", "1.0.0");
        }};

        Set<String> expressions = new HashSet<>(Arrays.asList("expr1", "expr2"));

        
        CqlEvaluationRequests actual = evaluator.getFilteredRequests(
                requests,
                libs,
                expressions
        );

        assertEquals(2, actual.getEvaluations().size());
        for (CqlEvaluationRequest evaluation : actual.getEvaluations()) {
            assertEquals(2, evaluation.getExpressions().size());
            for (String expressionName : evaluation.getExpressionNames()) {
                assertTrue(expressions.contains(expressionName));
            }
        }
    }

    @Test
    public void testGetFilteredRequestsGlobalParametersApplied() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();
        requests.setGlobalParameters(
                new HashMap<String, Parameter>() {{
                    put("param1", new IntegerParameter(10));
                    put("param2", new StringParameter("10"));
                    put("globalParam", new DecimalParameter("10.0"));
                }}  
        );

        CqlEvaluationRequest request = makeEvaluationRequest("context", "lib1", "1.0.0");
        request.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohortOrig")));
        request.setParameters(
                new HashMap<String, Parameter>() {{
                    put("param1", new IntegerParameter(1));
                    put("param2", new StringParameter("1"));
                    put("param3", new DecimalParameter("1.0"));
                }}
        );

        CqlEvaluationRequest request2 = makeEvaluationRequest("context", "lib2", "1.0.0");
        request2.setExpressionsByNames(new HashSet<>(Collections.singletonList("cohortOrig")));
        
        List<CqlEvaluationRequest> evaluations = Arrays.asList(request, request2);

        requests.setEvaluations(evaluations);

        CqlEvaluationRequests actual = evaluator.getFilteredRequests(requests, null, null);

        for (CqlEvaluationRequest evaluation : actual.getEvaluations()) {
            if (evaluation.getDescriptor().getLibraryId().equals("lib1")) {
                assertEquals(4, evaluation.getParameters().size());
                assertEquals(new IntegerParameter(1), evaluation.getParameters().get("param1"));
                assertEquals(new StringParameter("1"), evaluation.getParameters().get("param2"));
                assertEquals(new DecimalParameter("1.0"), evaluation.getParameters().get("param3"));
                assertEquals(new DecimalParameter("10.0"), evaluation.getParameters().get("globalParam"));
            }
            else if (evaluation.getDescriptor().getLibraryId().equals("lib2")) {
                assertEquals(3, evaluation.getParameters().size());
                assertEquals(new IntegerParameter(10), evaluation.getParameters().get("param1"));
                assertEquals(new StringParameter("10"), evaluation.getParameters().get("param2"));
                assertEquals(new DecimalParameter("10.0"), evaluation.getParameters().get("globalParam"));
            }
            else {
                fail("Unexpected library encountered. Expected only lib1 and lib2.");
            }
        }
    }

    @Test
    public void testReadCqlJobsSuccess() throws Exception {
        IntervalParameter measurementPeriod = new IntervalParameter();
        measurementPeriod.setStart(new DateParameter("2020-01-01")).setEnd(new DateParameter("2021-01-01"));
        
        IntegerParameter minimumAge = new IntegerParameter(17);
        
        evaluator.hadoopConfiguration = new SerializableConfiguration(SparkHadoopUtil.get().conf());
        CqlEvaluationRequests requests = evaluator.readJobSpecification("src/test/resources/simple-job/cql-jobs.json");
        assertNotNull(requests);
        assertEquals(measurementPeriod, requests.getGlobalParameters().get("Measurement Period"));
        assertEquals(1, requests.getEvaluations().size());
        assertEquals(minimumAge, requests.getEvaluations().get(0).getParameters().get("MinimumAge"));
    }

    @Test
    public void testReadFilteredJobs() throws Exception {
        evaluator.args.jobSpecPath = "src/test/resources/column-mapping-validation/metadata/cql-jobs.json";

        evaluator.hadoopConfiguration = new SerializableConfiguration(SparkHadoopUtil.get().conf());
        CqlEvaluationRequests requests = evaluator.getFilteredJobSpecificationWithIds();
        assertNotNull(requests);
        assertEquals(3, requests.getEvaluations().size());
        assertEquals(1, (int) requests.getEvaluations().get(0).getId());
        assertEquals(2, (int) requests.getEvaluations().get(1).getId());
        assertEquals(3, (int) requests.getEvaluations().get(2).getId());
    }
    
    @Test
    public void testReadCqlJobsInvalid() throws Exception {
        evaluator.hadoopConfiguration = new SerializableConfiguration(SparkHadoopUtil.get().conf());
        assertThrows(IllegalArgumentException.class,
                () -> evaluator.readJobSpecification("src/test/resources/invalid/cql-jobs-invalid-global.json"));
    }
    
    @Test
    public void testReadContextDefinitions() throws Exception {
        evaluator.hadoopConfiguration = new SerializableConfiguration(SparkHadoopUtil.get().conf());
        ContextDefinitions contextDefinitions = evaluator.readContextDefinitions("src/test/resources/alltypes/metadata/context-definitions.json");
        assertNotNull(contextDefinitions);
        assertEquals(5, contextDefinitions.getContextDefinitions().size());
        assertEquals(3, contextDefinitions.getContextDefinitions().get(0).getRelationships().size());
    }


    public static void assertStackTraceContainsMessage(Throwable th, String message) {
        boolean found = false;
        while( th != null ) {
            found = th.getMessage().contains(message);
            if( found ) {
                break;
            } else { 
                th = th.getCause();
            }
        }
        assertTrue( "Missing expected message", found );
    }
}
