package com.ibm.cohort.cql.spark.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;

public class OutputColumnNamer {
	public static OutputColumnNamer create(CqlEvaluationRequests evaluationRequests, String defaultNameDelimiter) {
		Map<String, List<CqlEvaluationRequest>> contextMap = new HashMap<>();
		
		// split out requests by context
		for (CqlEvaluationRequest evaluation : evaluationRequests.getEvaluations()) {
			List<CqlEvaluationRequest> requests = contextMap.computeIfAbsent(evaluation.getContextKey(), x -> new ArrayList<>());
			requests.add(evaluation);
		}
		
		
		
		return new OutputColumnNamer();
	}
	
	private static void createNameMapForContext(List<CqlEvaluationRequest> contextRequests, SparkOutputColumnEncoder defaultEncoder) {
		
	}
	
	private final Map<CqlEvaluationRequest, Map<String, String>> outputColumnLookup;
	
	private OutputColumnNamer() {
		outputColumnLookup = new HashMap<>();
	}
	
	public String getColumnName(CqlEvaluationRequest evaluationRequest) {
		return "";
	}
}
