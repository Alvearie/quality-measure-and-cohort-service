package com.ibm.cohort.cql.spark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.fs.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinitions;
import com.ibm.cohort.cql.spark.data.Patient;
import com.ibm.cohort.cql.spark.data.SparkTypeConverter;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;

public class SparkS3CqlEvaluatorTest extends BaseSparkTest {
    private static final long serialVersionUID = 1L;
    
    private SparkS3CqlEvaluator evaluator;
    
    @Before
    public void setUp() {
        this.evaluator = new SparkS3CqlEvaluator();
    }

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
        
        Java8API useJava8API = Java8API.ENABLED;
        try( SparkSession spark = initializeSession(useJava8API) ) {
            Dataset<Row> dataset = spark.createDataFrame(sourceData, Patient.class);
            
            Path tempFile = Paths.get("src/test/resources/testdata/simple", "patient" );
            dataset.write().format("delta").save(tempFile.toString());
        }
    }
    
    @Test
    public void testReadAggregateSuccess() throws Exception {

        String [] args = new String[] {
          "-d", "src/test/resources/jobs/simple/context-definitions.json",
          "-j", "src/test/resources/jobs/simple/cql-jobs.json",
          "-m", "src/test/resources/modelinfo/mock-modelinfo-1.0.0.xml",
          "-c", "src/test/resources/cql",
          "-i", "Patient=" + new File("src/test/resources/testdata/simple/patient").toURI().toString(),
          "-o", "Patient=" + new File("target/output/patient_cohort").toURI().toString()
        };
        
        Java8API useJava8API = Java8API.ENABLED;
        try( SparkSession spark = initializeSession(useJava8API) ) {
            evaluator.typeConverter = new SparkTypeConverter(useJava8API.getValue());
            
            SparkS3CqlEvaluator.main(args);
        }
    }

    @Test
    public void testReadCqlJobs() throws Exception {
        CqlEvaluationRequests requests = evaluator.readJobSpecification("src/test/resources/jobs/hi-example/cql-jobs.json");
        assertNotNull(requests);
        assertEquals(1, requests.getEvaluations().size());
        assertEquals(1, requests.getGlobalParameters().size());
    }
    
    @Test
    public void testReadContextDefinitions() throws Exception {
        ContextDefinitions contextDefinitions = evaluator.readContextDefinitions("src/test/resources/jobs/hi-example/context-definitions.json");
        assertNotNull(contextDefinitions);
        assertEquals(4, contextDefinitions.getContextDefinitions().size());
        assertEquals(4, contextDefinitions.getContextDefinitions().get(0).getRelationships().size());
    }
    
    protected CqlLibraryProvider getTestLibraryProvider() throws IOException, FileNotFoundException {
        CqlToElmTranslator translator = new CqlToElmTranslator();
        try( Reader r = new FileReader(new File("src/test/resources/modelinfo/mock-modelinfo-1.0.0.xml") ) ) {
            translator.registerModelInfo(r);
        }
        
        CqlLibraryProvider libraryProvider = new DirectoryBasedCqlLibraryProvider(new File("src/test/resources/cql"));
        CqlLibraryProvider translatingProvider = new TranslatingCqlLibraryProvider(libraryProvider, translator);
        return translatingProvider;
    }
}
