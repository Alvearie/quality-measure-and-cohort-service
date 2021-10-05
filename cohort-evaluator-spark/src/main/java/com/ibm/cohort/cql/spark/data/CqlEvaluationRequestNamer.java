package com.ibm.cohort.cql.spark.data;

import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;

public class CqlEvaluationRequestNamer {
	public static Map<String, String> getDefineToOutputNameMap(CqlEvaluationRequest request, String defaultColumnDelimiter) {
		HashMap<String, String> defineToOutputNameMap = new HashMap<>();
		
		DefaultSparkOutputColumnEncoder encoder = new DefaultSparkOutputColumnEncoder(defaultColumnDelimiter);
		
		String libraryId = request.getDescriptor().getLibraryId();
		for (String expression : request.getExpressions()) {
			if (defineToOutputNameMap.containsKey(expression)) {
				throw new IllegalArgumentException("Evaluation request contains duplicate expression " + expression + " for the library " + libraryId);
			}
			defineToOutputNameMap.put(expression, encoder.getColumnName(libraryId, expression));
		}

		return defineToOutputNameMap;
	}
	
}
