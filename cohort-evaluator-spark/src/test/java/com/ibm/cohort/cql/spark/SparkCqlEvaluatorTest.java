package com.ibm.cohort.cql.spark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.JavaTypeInference;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinitions;
import com.ibm.cohort.cql.spark.data.Patient;
import com.ibm.cohort.cql.spark.data.SparkTypeConverter;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;

import scala.Tuple2;

public class SparkCqlEvaluatorTest extends BaseSparkTest {
    private static final long serialVersionUID = 1L;
    
    private SparkCqlEvaluator evaluator;
    
    @Before
    public void setUp() {
        this.evaluator = new SparkCqlEvaluator();
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
        
        Java8API useJava8API = Java8API.ENABLED;
        try( SparkSession spark = initializeSession(useJava8API) ) {
            Dataset<Row> dataset = spark.createDataFrame(sourceData, Patient.class);
            
            Path tempFile = Paths.get("src/test/resources/simple-job/testdata", "patient" );
            dataset.write().format("delta").save(tempFile.toString());
        }
    }
    
    @Test
    public void testReadAggregateSuccess() throws Exception {

        String [] args = new String[] {
          "-d", "src/test/resources/simple-job/context-definitions.json",
          "-j", "src/test/resources/simple-job/cql-jobs.json",
          "-m", "src/test/resources/simple-job/modelinfo/simple-modelinfo-1.0.0.xml",
          "-c", "src/test/resources/simple-job/cql",
          "-i", "Patient=" + new File("src/test/resources/simple-job/testdata/patient").toURI().toString(),
          "-o", "Patient=" + new File("target/output/simple-job/patient_cohort").toURI().toString()
        };
        
        Java8API useJava8API = Java8API.ENABLED;
        try( SparkSession spark = initializeSession(useJava8API) ) {
            evaluator.typeConverter = new SparkTypeConverter(useJava8API.getValue());
            
            SparkCqlEvaluator.main(args);
        }
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
          "-d", "src/test/resources/alltypes/context-definitions.json",
          "-j", "src/test/resources/alltypes/cql-jobs.json",
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
          "-n", "10"
        };
        
        Java8API useJava8API = Java8API.ENABLED;
        try( SparkSession spark = initializeSession(useJava8API) ) {
            evaluator.typeConverter = new SparkTypeConverter(useJava8API.getValue());
            
            SparkCqlEvaluator.main(args);
        }
    }

    @Test
    public void testReadCqlJobs() throws Exception {
        CqlEvaluationRequests requests = evaluator.readJobSpecification("src/test/resources/alltypes/cql-jobs.json");
        assertNotNull(requests);
        assertEquals(5, requests.getEvaluations().size());
        assertNull(requests.getGlobalParameters());
    }
    
    @Test
    public void testReadContextDefinitions() throws Exception {
        ContextDefinitions contextDefinitions = evaluator.readContextDefinitions("src/test/resources/alltypes/context-definitions.json");
        assertNotNull(contextDefinitions);
        assertEquals(5, contextDefinitions.getContextDefinitions().size());
        assertEquals(3, contextDefinitions.getContextDefinitions().get(0).getRelationships().size());
    }
    
    protected CqlLibraryProvider getTestLibraryProvider() throws IOException, FileNotFoundException {
        CqlToElmTranslator translator = new CqlToElmTranslator();
        try( Reader r = new FileReader(new File("src/test/resources/alltypes/modelinfo/mock-modelinfo-1.0.0.xml") ) ) {
            translator.registerModelInfo(r);
        }
        
        CqlLibraryProvider libraryProvider = new DirectoryBasedCqlLibraryProvider(new File("src/test/resources/alltypes/cql"));
        CqlLibraryProvider translatingProvider = new TranslatingCqlLibraryProvider(libraryProvider, translator);
        return translatingProvider;
    }
    
    @Test
    public void testManualSchemaCreation() {
        try( SparkSession spark = initializeSession(Java8API.ENABLED) ) {
          SortedMap<String,Object> columnData = new TreeMap<>();
          columnData.put("Greeting", "Hello,World");
          columnData.put("Age", 40);
          columnData.put("Weight", 145.97);
          
          Map<String,Object> nested = new HashMap<>();
          nested.put("_type", "time");
          nested.put("value", "10:11:12");
          //columnData.put("NestedMap", JavaConverters.mapAsScalaMap(nested));
          //columnData.put("NestedMap", nested);
          
          List<Object> rowData = new ArrayList<>();
          
          StructType schema = new StructType();
          for( Map.Entry<String,Object> entry : columnData.entrySet() ) {
              String key = entry.getKey();
              Object value = entry.getValue();
                         
              Tuple2<DataType, Object> tuple2 = JavaTypeInference.inferDataType(value.getClass());
              schema = schema.add( key, tuple2._1() );
              rowData.add(value);
          }
          
          Row row = RowFactory.create(rowData.toArray(new Object[rowData.size()]));
          
          Dataset<Row> dataset = spark.createDataFrame(Arrays.asList(row), schema);
          assertEquals(1, dataset.count());
          assertEquals(columnData.keySet(), Arrays.stream(dataset.schema().fieldNames()).collect(Collectors.toSet()));
          
          Row first = ((Row[])dataset.take(1))[0];
          for( int i=0; i<dataset.schema().fieldNames().length; i++ ) {
              assertEquals( columnData.get(dataset.schema().fieldNames()[i]), first.getAs(i) );
          }
        }
    }
}
