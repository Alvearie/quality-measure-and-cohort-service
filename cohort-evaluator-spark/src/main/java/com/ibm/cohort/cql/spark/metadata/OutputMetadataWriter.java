/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metadata;

public interface OutputMetadataWriter {
	void writeMetadata(EvaluationSummary evaluationSummary);
}
