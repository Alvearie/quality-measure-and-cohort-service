/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * Retrieves {@link Dataset}s
 */
public interface DatasetRetriever {

    /**
     * Reads a {@link Dataset} for a provided path.
     *
     * @param dataType A typeName for a CQL or custom model type
     * @param path A Hadoop compatible path/URI.
     * @return The data in {@link Dataset} form.
     */
    Dataset<Row> readDataset(String dataType, String path);

}
