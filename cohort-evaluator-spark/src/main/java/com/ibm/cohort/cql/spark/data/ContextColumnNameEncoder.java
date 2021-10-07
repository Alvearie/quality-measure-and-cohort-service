package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlExpressionConfiguration;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

/**
 * Calculates output column names for expressions defined in a List of CqlEvaluationRequest objects.
 * Each output column name is either explicitly configured on CqlEvaluationRequest,
 * or is calculated using a default behavior.
 *
 * Output names (whether configured or created with a default behavior) must be
 * unique or else an IllegalArgumentException is thrown.
 */
public class ContextColumnNameEncoder implements SparkOutputColumnEncoder, Serializable {
	public static ContextColumnNameEncoder create(List<CqlEvaluationRequest> contextRequests, String defaultColumnDelimiter) {
		Map<CqlEvaluationRequest, Map<String, String>> requestToDefineToOutputColumn = new HashMap<>();
		Set<String> outputColumnNames = new HashSet<>();
		
		for (CqlEvaluationRequest contextRequest : contextRequests) {
			Map<String, String> defineToOutputNameMap = getDefineToOutputNameMap(contextRequest, defaultColumnDelimiter);
			
			// Make sure output names are unique across requests
			for (String value : defineToOutputNameMap.values()) {
				if (outputColumnNames.contains(value)) {
					throw new IllegalArgumentException("Duplicate outputColumn " + value + " defined in the job definition file.");
				}
				outputColumnNames.add(value);
			}

			requestToDefineToOutputColumn.put(contextRequest, defineToOutputNameMap);
		}
		
		return new ContextColumnNameEncoder(requestToDefineToOutputColumn, outputColumnNames);
	}
	
	protected static Map<String, String> getDefineToOutputNameMap(CqlEvaluationRequest request, String defaultColumnDelimiter) {
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
				outputColumn = encoder.getColumnName(request, expressionName);
			}
			if (outputColumns.contains(outputColumn)) {
				throw new IllegalArgumentException("Evaluation request contains duplicate outputColumn " + outputColumn);
			}

			outputColumns.add(outputColumn);
			defineToOutputNameMap.put(expressionName, outputColumn);
		}

		return defineToOutputNameMap;
	}
	
	private final Map<CqlEvaluationRequest, Map<String, String>> requestToDefineToOutputColumn;
	private final Set<String> outputColumnNames;
	
	private ContextColumnNameEncoder(Map<CqlEvaluationRequest, Map<String, String>> requestToDefineToOutputColumn, Set<String> outputColumnNames) {
		this.requestToDefineToOutputColumn = requestToDefineToOutputColumn;
		this.outputColumnNames = outputColumnNames;
	}
	
	public Set<String> getOutputColumnNames() {
		return outputColumnNames;
	}

	@Override
	public String getColumnName(CqlEvaluationRequest request, String defineName) {
		Map<String, String> defineToOutputName = requestToDefineToOutputColumn.get(request);
		if (defineToOutputName == null) {
			// TODO: Figure out how to handle this more gracefully. What can't be found?
			throw new IllegalArgumentException("Cannot find column name data for the provided request.");
		}
		return defineToOutputName.get(defineName);
	}
}
