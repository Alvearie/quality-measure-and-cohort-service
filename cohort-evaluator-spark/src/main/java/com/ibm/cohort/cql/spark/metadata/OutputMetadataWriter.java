package com.ibm.cohort.cql.spark.metadata;

public interface OutputMetadataWriter {
	void writeMetadata(EvaluationSummary evaluationSummary);
}
