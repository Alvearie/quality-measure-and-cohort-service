/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

// TODO: Javadoc
public interface DatasetRetriever {

    Dataset<Row> readDataset(String path);

}
