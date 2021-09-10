/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultDatasetRetrieverTest {

    private static final String PARQUET_FILE = "src/test/resources/alltypes/testdata/test-A.parquet";
    private static final String DELTA_FILE = "src/test/resources/simple-job/testdata/patient";

    private static SparkSession spark;

    @BeforeClass
    public static void initialize() {
        spark = SparkSession.builder()
                .master("local[1]")
                .config("spark.sql.shuffle.partitions", 1)
                .getOrCreate();
    }

    @AfterClass
    public static void shutdown() {
        spark.stop();
        spark = null;
    }

    @Test
    public void readDataset_defaultFormat_parquet() {
        DatasetRetriever datasetRetriever = new DefaultDatasetRetriever(spark);
        Dataset<Row> dataset = datasetRetriever.readDataset(PARQUET_FILE);
        Assert.assertEquals(572, dataset.count());
    }

    @Test
    public void readDataset_explicitFormat_deltaLake() {
        DatasetRetriever datasetRetriever = new DefaultDatasetRetriever(spark, "delta");
        Dataset<Row> dataset = datasetRetriever.readDataset(DELTA_FILE);
        Assert.assertEquals(10, dataset.count());
    }

}
