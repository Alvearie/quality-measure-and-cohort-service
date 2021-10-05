package com.ibm.cohort.cql.spark.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;

public class ContextColumnNamer {
	// assume a single context of cqlevaluationrequests
	
	public static ContextColumnNamer create(List<CqlEvaluationRequest> contextRequests, String defaultColumnDelimiter) {

		DefaultSparkOutputColumnEncoder defaultEncoder = new DefaultSparkOutputColumnEncoder(defaultColumnDelimiter);
		
		Map<String, Integer> nameCounts = new HashMap<>();
		Map<String, String> defineToOutputNames = new HashMap<>();

		for (CqlEvaluationRequest contextRequest : contextRequests) {
			Set<String> expressions = contextRequest.getExpressions();
			for (String expression : expressions) {
				String columnName = defaultEncoder.getColumnName(contextRequest.getDescriptor().getLibraryId(), expression);
				Integer integer = nameCounts.get(columnName);
				if (integer == null) {
					// todo: name not seen yet, just use it
					
				}
				else {
					// todo: find number to append to end
				}
			}
		}


		return new ContextColumnNamer();
	}
	
	private ContextColumnNamer() {
		
	}
}
