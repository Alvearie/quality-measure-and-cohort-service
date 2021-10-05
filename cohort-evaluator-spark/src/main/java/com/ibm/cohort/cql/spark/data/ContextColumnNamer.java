package com.ibm.cohort.cql.spark.data;

import java.util.List;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;

public class ContextColumnNamer {
	// assume a single context of cqlevaluationrequests
	
	public static ContextColumnNamer create(List<CqlEvaluationRequest> contextRequests, String defaultColumnDelimiter) {
		

		return new ContextColumnNamer();
	}
	
	private ContextColumnNamer() {
		
	}
}
