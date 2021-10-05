package com.ibm.cohort.cql.spark.data;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;

public class OutputColumnNamer {
	public static OutputColumnNamer create(CqlEvaluationRequests evaluationRequests, String defaultNameDelimiter) {

		return new OutputColumnNamer();
	}
	
}
