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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.evaluation.parameters.DateParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntegerParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntervalParameter;
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
        String [] args = new String[] {
          "-d", "src/test/resources/simple-job/context-definitions.json",
          "-j", "src/test/resources/simple-job/cql-jobs.json",
          "-m", "src/test/resources/simple-job/modelinfo/simple-modelinfo-1.0.0.xml",
          "-c", "src/test/resources/simple-job/cql",
          "-i", "Patient=" + new File("src/test/resources/simple-job/testdata/patient").toURI().toString(),
          "-o", "Patient=" + new File("target/output/simple-job/patient_cohort").toURI().toString(),
          "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);
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
          "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);
    }
    
    @Test
    public void testValidateOutput() throws Exception {
        File inputDir = new File("src/test/resources/output-validation/");
        File outputDir = new File("target/output/output-validation/");

        File context1IdFile = new File(outputDir, "context-1-id");
        File context2IdFile = new File(outputDir, "context-2-id");

        String [] args = new String[] {
                "-d", "src/test/resources/output-validation/metadata/context-definitions.json",
                "-j", "src/test/resources/output-validation/metadata/cql-jobs.json",
                "-m", "src/test/resources/output-validation/modelinfo/simple-all-types-model-info.xml",
                "-c", "src/test/resources/output-validation/cql",
                "--input-format", "parquet",
                "-i", "Type1=" + new File(inputDir, "testdata/Type1").toURI().toString(),
                "-i", "Type2=" + new File(inputDir, "testdata/Type2").toURI().toString(),
                "-o", "Context1Id=" + context1IdFile.toURI().toString(),
                "-o", "Context2Id=" + context2IdFile.toURI().toString(),
                "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);

        validateOutputCountsAndColumns(
                context1IdFile.toURI().toString(),
                new HashSet<>(Arrays.asList("id", "Context1Id.define_integer", "Context1Id.define_decimal", "Context1Id.define_string", "Context1Id.define_boolean")),
                5
        );

        validateOutputCountsAndColumns(
                context2IdFile.toURI().toString(),
                new HashSet<>(Arrays.asList("id", "Context2Id.define_date", "Context2Id.define_datetime")),
                5
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownDefineThrowsException() throws Exception {
        File inputDir = new File("src/test/resources/alltypes/");
        File outputDir = new File("target/output/alltypes/");

        File aFile = new File(outputDir, "A_cohort");

        String [] args = new String[] {
                "-d", "src/test/resources/alltypes/metadata/context-definitions.json",
                // cql-jobs file references an unknown define in its evaluation request
                "-j", "src/test/resources/unknown-define/metadata/cql-jobs.json",
                "-m", "src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml",
                "-c", "src/test/resources/alltypes/cql",
                "--input-format", "parquet",
                "-i", "A=" + new File(inputDir, "testdata/test-A.parquet").toURI().toString(),
                "-o", "A=" + aFile.toURI().toString(),
                "-n", "10",
                "--overwrite-output-for-contexts"
        };

        SparkCqlEvaluator.main(args);
    }
    
    // TODO RIP
    private void validateOutputCountsAndColumns(String filename, Set<String> columnNames, int numExpectedRows) {
        SparkSession sparkSession = initializeSession(Java8API.ENABLED);
        Dataset<Row> results = sparkSession.read().parquet(filename);
        validateColumnNames(results.schema(), columnNames);
        
        assertEquals(numExpectedRows, results.count());
    }
    
    // TODO RIP
    private void validateColumnNames(StructType schema, Set<String> columnNames) {
        for (String field : schema.fieldNames()) {
            assertTrue(columnNames.contains(field));
        }
        assertEquals(columnNames.size(), schema.fields().length);
    }

    private CqlEvaluationRequest makeEvaluationRequest(String contextName) {
        return makeEvaluationRequest(contextName, null, null);
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
    public void testFilterRequestsNoEvaluationRequests() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();

        List<CqlEvaluationRequest> cqlEvaluationRequests = evaluator.filterRequests(requests, "A");

        assertTrue(cqlEvaluationRequests.isEmpty());
    }
    
    @Test
    public void testFilterRequestsNoEvaluationRequestsForContext() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();

        List<CqlEvaluationRequest> evaluations = Arrays.asList(
                makeEvaluationRequest("B"),
                makeEvaluationRequest("C"),
                makeEvaluationRequest("D")
        );
        
        requests.setEvaluations(evaluations);
        
        List<CqlEvaluationRequest> cqlEvaluationRequests = evaluator.filterRequests(requests, "A");
        
        assertTrue(cqlEvaluationRequests.isEmpty());
    }

    @Test
    public void testFilterRequestsFilterToRequestsForContext() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();

        List<CqlEvaluationRequest> evaluations = Arrays.asList(
                makeEvaluationRequest("A"),
                makeEvaluationRequest("A"),
                makeEvaluationRequest("B")
        );

        requests.setEvaluations(evaluations);

        List<CqlEvaluationRequest> cqlEvaluationRequests = evaluator.filterRequests(requests, "A");

        assertEquals(2, cqlEvaluationRequests.size());
        for (CqlEvaluationRequest cqlEvaluationRequest : cqlEvaluationRequests) {
            assertEquals("A", cqlEvaluationRequest.getContextKey());
        }
    }

    @Test
    public void testFilterByLibraryNameNoMatches() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();

        List<CqlEvaluationRequest> evaluations = Arrays.asList(
                makeEvaluationRequest("A", "lib4", "1.0.0"),
                makeEvaluationRequest("A", "lib3", "3.0.0"),
                makeEvaluationRequest("B", "lib2", "1.0.0")
        );

        requests.setEvaluations(evaluations);

        args.libraries.put("lib1", "1.0.0");
        args.libraries.put("lib2", "1.0.0");

        List<CqlEvaluationRequest> cqlEvaluationRequests = evaluator.filterRequests(requests, "A");

        assertTrue(cqlEvaluationRequests.isEmpty());
    }

    @Test
    public void testFilterByLibraryNameMatchesIgnoresVersion() {
        CqlEvaluationRequests requests = new CqlEvaluationRequests();

        Set<CqlEvaluationRequest> expectedRequests = new HashSet<>();
        expectedRequests.add(makeEvaluationRequest("A", "lib1", "4.0.0"));
        expectedRequests.add(makeEvaluationRequest("A", "lib2", "7.0.0"));

        List<CqlEvaluationRequest> evaluations = new ArrayList<>(expectedRequests);
        evaluations.add(makeEvaluationRequest("B", "lib2", "1.0.0"));

        requests.setEvaluations(evaluations);

        args.libraries.put("lib1", "1.0.0");
        args.libraries.put("lib2", "1.0.0");

        List<CqlEvaluationRequest> cqlEvaluationRequests = evaluator.filterRequests(requests, "A");

        assertEquals(2, cqlEvaluationRequests.size());
        assertTrue(expectedRequests.containsAll(cqlEvaluationRequests));
    }

    @Test
    public void testReadCqlJobsSuccess() throws Exception {
        IntervalParameter measurementPeriod = new IntervalParameter();
        measurementPeriod.setStart(new DateParameter("2020-01-01")).setEnd(new DateParameter("2021-01-01"));
        
        IntegerParameter minimumAge = new IntegerParameter(17);
        
        CqlEvaluationRequests requests = evaluator.readJobSpecification("src/test/resources/simple-job/cql-jobs.json");
        assertNotNull(requests);
        assertEquals(measurementPeriod, requests.getGlobalParameters().get("Measurement Period"));
        assertEquals(1, requests.getEvaluations().size());
        assertEquals(minimumAge, requests.getEvaluations().get(0).getParameters().get("MinimumAge"));
    }
    
    @Test
    public void testReadCqlJobsInvalid() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> evaluator.readJobSpecification("src/test/resources/invalid/cql-jobs-invalid-global.json"));
    }
    
    @Test
    public void testReadContextDefinitions() throws Exception {
        ContextDefinitions contextDefinitions = evaluator.readContextDefinitions("src/test/resources/alltypes/metadata/context-definitions.json");
        assertNotNull(contextDefinitions);
        assertEquals(5, contextDefinitions.getContextDefinitions().size());
        assertEquals(3, contextDefinitions.getContextDefinitions().get(0).getRelationships().size());
    }
    
}
