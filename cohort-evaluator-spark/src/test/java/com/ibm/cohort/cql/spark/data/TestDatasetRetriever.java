/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Map;

/**
 * A simple {@link DatasetRetriever} that stores precomputed mappings from
 * {@link String} to {@link Dataset}.
 */
public class TestDatasetRetriever implements DatasetRetriever {

    private final Map<String, Dataset<Row>> datasets;

    public TestDatasetRetriever(Map<String, Dataset<Row>> datasets) {
        this.datasets = datasets;
    }

    @Override
    public Dataset<Row> readDataset(String path) {
        return datasets.get(path);
    }

}
