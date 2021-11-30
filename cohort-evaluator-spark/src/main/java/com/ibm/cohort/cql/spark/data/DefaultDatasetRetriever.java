/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * A {@link DatasetRetriever} that reads data from a provided {@link SparkSession}.
 */
public class DefaultDatasetRetriever implements DatasetRetriever {

    private final SparkSession spark;
    private final String inputFormat;

    /**
     * @param spark The {@link SparkSession} to read from.
     */
    public DefaultDatasetRetriever(SparkSession spark) {
        this(spark, null);
    }

    /**
     * @param spark The {@link SparkSession} to read from.
     * @param inputFormat The Spark input format to use for all read operations.
     */
    public DefaultDatasetRetriever(SparkSession spark, String inputFormat) {
        this.spark = spark;
        this.inputFormat = inputFormat;
    }

    @Override
    public Dataset<Row> readDataset(String path) {
        DataFrameReader reader = spark.read();
        if( inputFormat != null ) {
            reader = reader.format(inputFormat);
        }
        return reader.load(path);
    }
}
