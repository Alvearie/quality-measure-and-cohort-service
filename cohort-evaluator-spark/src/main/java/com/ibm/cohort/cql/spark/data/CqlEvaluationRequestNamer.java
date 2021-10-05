package com.ibm.cohort.cql.spark.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlExpressionConfiguration;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

/**
 * Calculate a map of define name to output column name for each 
 * CqlExpressionConfiguration in a single CqlEvaluationRequest.
 * If no output column name is specified for a CqlExpressionConfiguration,
 * calculate a name using the DefaultSparkOutputColumnEncoder.
 */
public class CqlEvaluationRequestNamer {
	public static Map<String, String> getDefineToOutputNameMap(CqlEvaluationRequest request, String defaultColumnDelimiter) {
		HashMap<String, String> defineToOutputNameMap = new HashMap<>();
		
		DefaultSparkOutputColumnEncoder encoder = new DefaultSparkOutputColumnEncoder(defaultColumnDelimiter);
		
		Set<String> outputColumns = new HashSet<>();

		CqlLibraryDescriptor descriptor = request.getDescriptor();
		if (descriptor == null || descriptor.getLibraryId() == null || descriptor.getLibraryId() == "") {
			throw new IllegalArgumentException("A library descriptor must be defined for each configured evaluation request.");
		}
		
		String libraryId = descriptor.getLibraryId();
		for (CqlExpressionConfiguration expression : request.getExpressions()) {
			String expressionName = expression.getName();
			String outputColumn = expression.getoutputColumn();

			if (defineToOutputNameMap.containsKey(expressionName)) {
				throw new IllegalArgumentException("Evaluation request contains duplicate expression " + expressionName + " for the library " + libraryId);
			}
			if (outputColumn == null) {
				outputColumn = encoder.getColumnName(libraryId, expressionName);
			}
			if (outputColumns.contains(outputColumn)) {
				throw new IllegalArgumentException("Evaluation request contains duplicate outputColumn " + outputColumn);
			}

			outputColumns.add(outputColumn);
			defineToOutputNameMap.put(expressionName, outputColumn);
		}

		return defineToOutputNameMap;
	}
	
}
