/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.cohort.cql.spark.BaseSparkTest;

public class DefaultDatasetRetrieverTest extends BaseSparkTest {

    private static final String PARQUET_FILE = "src/test/resources/alltypes/testdata/test-A.parquet";
    private static final String DELTA_FILE = "src/test/resources/simple-job/testdata/patient";

    private static SparkSession spark;

    @BeforeClass
    public static void initialize() {
        spark = initializeSession();
    }

    @Test
    public void readDataset_defaultFormat_deltaLake() {
        DatasetRetriever datasetRetriever = new DefaultDatasetRetriever(spark);
        Dataset<Row> dataset = datasetRetriever.readDataset("Dummy", DELTA_FILE);
        long actual = dataset.count();
        Assert.assertEquals(10L, actual);
    }

    @Test
    public void readDataset_explicitFormat_parquet() {
        DatasetRetriever datasetRetriever = new DefaultDatasetRetriever(spark, "parquet");
        Dataset<Row> dataset = datasetRetriever.readDataset("Dummy", PARQUET_FILE);
        long actual = dataset.count();
        Assert.assertEquals(572L, actual);
    }

}
