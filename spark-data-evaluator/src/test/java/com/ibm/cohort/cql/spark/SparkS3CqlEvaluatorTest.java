package com.ibm.cohort.cql.spark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.cql.spark.data.AllTypesJava8DatesPOJO;

public class SparkS3CqlEvaluatorTest extends BaseSparkTest {

    private SparkS3CqlEvaluator evaluator;
    
    @Before
    public void setUp() {
        this.evaluator = new SparkS3CqlEvaluator();
    }
    
    @Test
    public void testReadAndAggregateDatasetSuccess() throws Exception {
        int rowCount = 10;
        int groups = 2;
        
        evaluator.aggregateOnContext = true;
        
        try( SparkSession spark = initializeSession(Java8API.ENABLED) ) {
            
            List<AllTypesJava8DatesPOJO> sourceData = new ArrayList<>();
            for(int i=0; i<rowCount; i++) {
                AllTypesJava8DatesPOJO pojo = AllTypesJava8DatesPOJO.randomInstance();
                pojo.setIntegerField( i % groups );
                sourceData.add( pojo );
            }
            
            Dataset<Row> dataset = spark.createDataFrame(sourceData, AllTypesJava8DatesPOJO.class);
            
            Path tempDir = Files.createTempDirectory(Paths.get("target"), "testdata");
            Path tempFile = Paths.get(tempDir.toString(), "patient" );
            dataset.write().parquet(tempFile.toString());
            
            JavaPairRDD<Object,Row> data = evaluator.readDataset(spark, tempFile.toString(), "AllTypesJava8DatesPOJO", "integerField");
            assertEquals( sourceData.size(), data.count() );
            
            Row row = data.take(1).get(0)._2();
            assertFalse( row.schema().getFieldIndex(SparkS3CqlEvaluator.SOURCE_FACT_IDX).isEmpty() );
            assertEquals( "AllTypesJava8DatesPOJO", row.getAs(SparkS3CqlEvaluator.SOURCE_FACT_IDX) );
            
            JavaPairRDD<Object,List<Row>> aggregated = evaluator.aggregateByContext(data);
            assertEquals( groups, aggregated.count() );
            
            List<Row> rows = aggregated.take(1).get(0)._2();
            assertEquals( rowCount / groups, rows.size() );
        }
    }
}
