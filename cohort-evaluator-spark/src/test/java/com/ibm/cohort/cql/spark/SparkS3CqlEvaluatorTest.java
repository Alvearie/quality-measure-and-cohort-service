package com.ibm.cohort.cql.spark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.fs.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.spark.data.Patient;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;

import scala.Tuple2;

public class SparkS3CqlEvaluatorTest extends BaseSparkTest {

    private SparkS3CqlEvaluator evaluator;
    
    @Before
    public void setUp() {
        this.evaluator = new SparkS3CqlEvaluator();
    }
    
    @Test
    public void testReadAggregateSuccess() throws Exception {
        int rowCount = 10;
        int groups = 2;
        
        evaluator.aggregateOnContext = true;
        evaluator.debug = true;
        
        try( SparkSession spark = initializeSession(Java8API.ENABLED) ) {
            
            List<Patient> sourceData = new ArrayList<>();
            for(int i=0; i<rowCount; i++) {
                Patient pojo = Patient.randomInstance();
                pojo.setId( String.valueOf(i % groups) );
                sourceData.add( pojo );
            }
            
            Dataset<Row> dataset = spark.createDataFrame(sourceData, Patient.class);
            
            Path tempDir = Files.createTempDirectory(Paths.get("target"), "testdata");
            Path tempFile = Paths.get(tempDir.toString(), "patient" );
            dataset.write().parquet(tempFile.toString());
            
            JavaPairRDD<Object,Row> data = evaluator.readDataset(spark, tempFile.toString(), "Patient", "id");
            assertEquals( sourceData.size(), data.count() );
            
            Row row = data.take(1).get(0)._2();
            assertFalse( row.schema().getFieldIndex(SparkS3CqlEvaluator.SOURCE_FACT_IDX).isEmpty() );
            assertEquals( "Patient", row.getAs(SparkS3CqlEvaluator.SOURCE_FACT_IDX) );
            
            JavaPairRDD<Object,List<Row>> aggregated = evaluator.aggregateByContext(data);
            assertEquals( groups, aggregated.count() );
            
            Tuple2<Object,List<Row>> singleContext = aggregated.take(1).get(0);
            List<Row> rows = singleContext._2();
            assertEquals( rowCount / groups, rows.size() );
        }
    }
    
    @Test
    public void testReadNoAggregationEvaluationSuccess() throws Exception {
        int rowCount = 10;
        int groups = 2;
        
        evaluator.aggregateOnContext = false;
        evaluator.libraryId = "SampleLibrary";
        evaluator.libraryVersion = "1.0.0";
        
        try( SparkSession spark = initializeSession(Java8API.ENABLED) ) {
            
            List<Patient> sourceData = new ArrayList<>();
            for(int i=0; i<rowCount; i++) {
                Patient pojo = Patient.randomInstance();
                pojo.setId( String.valueOf(i % groups) );
                sourceData.add( pojo );
            }
            
            Dataset<Row> dataset = spark.createDataFrame(sourceData, Patient.class);
            
            Path tempDir = Files.createTempDirectory(Paths.get("target"), "testdata");
            Path tempFile = Paths.get(tempDir.toString(), "patient" );
            dataset.write().parquet(tempFile.toString());
            
            JavaPairRDD<Object,Row> data = evaluator.readDataset(spark, tempFile.toString(), "Patient", "id");
            assertEquals( sourceData.size(), data.count() );
            
            Row row = data.take(1).get(0)._2();
            assertFalse( row.schema().getFieldIndex(SparkS3CqlEvaluator.SOURCE_FACT_IDX).isEmpty() );
            assertEquals( "Patient", row.getAs(SparkS3CqlEvaluator.SOURCE_FACT_IDX) );
            
            JavaPairRDD<Object,List<Row>> aggregated = evaluator.aggregateByContext(data);
            assertEquals( sourceData.size(), aggregated.count() );

            Tuple2<Object,List<Row>> singleContext = aggregated.take(1).get(0);
            
            CqlToElmTranslator translator = new CqlToElmTranslator();
            try( Reader r = new FileReader(new File("src/test/resources/modelinfo/mock-modelinfo-1.0.0.xml") ) ) {
                translator.registerModelInfo(r);
            }
            
            CqlLibraryProvider libraryProvider = new DirectoryBasedCqlLibraryProvider(new File("src/test/resources/cql"));
            CqlLibraryProvider translatingProvider = new TranslatingCqlLibraryProvider(libraryProvider, translator);
            Tuple2<Object,Map<String,Object>> result = evaluator.evaluate(translatingProvider, singleContext);
            assertNotNull(result);
            assertEquals(singleContext._1(), result._1());
            
            String expectedGender = singleContext._2().get(0).getAs("gender");
            assertNotNull(expectedGender);
            
            Map<String,Object> defineResults = result._2();
            assertEquals(expectedGender.equals("female"), defineResults.get("IsFemale"));
        }
    }
    
    @Test
    public void testContextColumnIsConstant() {
        String expectedConstant = "constant";
        
        evaluator.contextColumn = expectedConstant;
        assertEquals( expectedConstant, evaluator.getContextColumnForDataType("sometype") ); 
        assertEquals( expectedConstant, evaluator.getContextColumnForDataType("othertype") );
    }
}
