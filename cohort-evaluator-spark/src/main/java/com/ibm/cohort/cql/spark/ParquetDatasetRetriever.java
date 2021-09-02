/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

// TODO: Javadoc
public class ParquetDatasetRetriever implements DatasetRetriever {

    private final SparkSession spark;

    public ParquetDatasetRetriever(SparkSession spark) {
        this.spark = spark;
    }

    @Override
    public Dataset<Row> readDataset(String path) {
        return spark.read().parquet(path);
    }

}
