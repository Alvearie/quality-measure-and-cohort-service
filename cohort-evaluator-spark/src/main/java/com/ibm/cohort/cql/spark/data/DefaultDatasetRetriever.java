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

public class DefaultDatasetRetriever implements DatasetRetriever {

    private final SparkSession spark;
    private final String inputFormat;

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
