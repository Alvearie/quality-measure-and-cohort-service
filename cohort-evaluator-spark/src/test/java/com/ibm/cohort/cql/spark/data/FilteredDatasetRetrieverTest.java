/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.cql.spark.BaseSparkTest;

public class FilteredDatasetRetrieverTest extends BaseSparkTest {
    private static final long serialVersionUID = 1L;
    private SparkSession spark;

    @Before
    public void setUp() {
        this.spark = initializeSession(Java8API.ENABLED);
    }
    
    @Test
    public void testColumnFiltering() {
        String dataType = "A";
        String path = new File("src/test/resources/alltypes/testdata/test-A.parquet").toURI().toString();
        
        DefaultDatasetRetriever defaultRetriever = new DefaultDatasetRetriever(spark, "parquet");
        Dataset<Row> baseline = defaultRetriever.readDataset(dataType, path);
        assertEquals(12, baseline.schema().fields().length);

        String colName = "boolean_col";
        
        Map<String,Set<String>> fieldsByDataType = new HashMap<>();
        fieldsByDataType.put(dataType, Collections.singleton(colName));
        
        FilteredDatasetRetriever filteredRetriever = new FilteredDatasetRetriever(defaultRetriever, fieldsByDataType);
        Dataset<Row> filtered = filteredRetriever.readDataset(dataType, path);
        assertEquals(1, filtered.schema().fields().length);
        assertEquals(0, filtered.schema().getFieldIndex(colName).get());
    }
    
    @Test
    public void testColumnFilteringNoColumnsRequired() {
        String dataType = "A";
        String path = new File("src/test/resources/alltypes/testdata/test-A.parquet").toURI().toString();
        
        Map<String,Set<String>> fieldsByDataType = new HashMap<>();
        
        DefaultDatasetRetriever defaultRetriever = new DefaultDatasetRetriever(spark, "parquet");
        FilteredDatasetRetriever filteredRetriever = new FilteredDatasetRetriever(defaultRetriever, fieldsByDataType);
        Dataset<Row> filtered = filteredRetriever.readDataset(dataType, path);
        assertNull(filtered);
    }
}
