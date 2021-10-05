package com.ibm.cohort.cql.spark.data;

import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlExpressionConfiguration;

public class CqlEvaluationRequestNamer {
	public static Map<String, String> getDefineToOutputNameMap(CqlEvaluationRequest request, String defaultColumnDelimiter) {
		HashMap<String, String> defineToOutputNameMap = new HashMap<>();
		
		DefaultSparkOutputColumnEncoder encoder = new DefaultSparkOutputColumnEncoder(defaultColumnDelimiter);
		
		String libraryId = request.getDescriptor().getLibraryId();
		for (CqlExpressionConfiguration expression : request.getExpressions()) {
			String expressionName = expression.getName();
			String outputColumn = expression.getoutputColumn();

			if (defineToOutputNameMap.containsKey(expressionName)) {
				throw new IllegalArgumentException("Evaluation request contains duplicate expression " + expressionName + " for the library " + libraryId);
			}

			defineToOutputNameMap.put(expressionName, outputColumn == null ? encoder.getColumnName(libraryId, expressionName) : outputColumn);
		}

		return defineToOutputNameMap;
	}
	
}
