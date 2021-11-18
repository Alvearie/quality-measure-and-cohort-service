/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlExpressionConfiguration;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.spark.util.CqlEvaluationRequestUtil;

/**
 * Calculates output column names for expressions defined in a List of CqlEvaluationRequest objects.
 * Each output column name is either explicitly configured on CqlEvaluationRequest,
 * or is calculated using a default behavior.
 *
 * Output names (whether configured or created with a default behavior) must be
 * unique or else an IllegalArgumentException is thrown.
 */
public class ContextColumnNameEncoder implements SparkOutputColumnEncoder {
    
    public static ContextColumnNameEncoder create(List<CqlEvaluationRequest> contextRequests, String defaultColumnDelimiter) {
        return create(contextRequests, null, defaultColumnDelimiter);
    }
    
	public static ContextColumnNameEncoder create(List<CqlEvaluationRequest> contextRequests, Set<String> keyParameterNames, String defaultColumnDelimiter) {
		Map<Integer, Map<String, String>> requestToDefineToOutputColumn = new HashMap<>();
		Map<String, Set<String>> outputColumnNamesByParameters = new HashMap<>();
		
		for (CqlEvaluationRequest contextRequest : contextRequests) {
		    //The global parameters need to be present in the request at this point. They are applied automatically by
		    //the getFilteredRequestsByContext() method, so we are good, but FYI.
		    String parametersJson = CqlEvaluationRequestUtil.getKeyParametersColumnData(contextRequest, keyParameterNames);
		    Set<String> outputColumnNames = outputColumnNamesByParameters.computeIfAbsent(parametersJson, x -> new HashSet<>());

		    Map<String, String> defineToOutputNameMap = getDefineToOutputNameMap(contextRequest, defaultColumnDelimiter);
			
			// Make sure output names per unique parameter combination are unique across requests
			for (String value : defineToOutputNameMap.values()) {
				if (outputColumnNames.contains(value)) {
					throw new IllegalArgumentException("Duplicate outputColumn " + value + " defined for context " + contextRequest.getContextKey());
				}
				outputColumnNames.add(value);
			}

			Integer requestId = contextRequest.getId();
			if (requestId == null) {
				throw new IllegalArgumentException("Each CqlEvaluationRequest argument must have an requestId set before calling ContextColumnNameEncoder.create()");
			}
			requestToDefineToOutputColumn.put(requestId, defineToOutputNameMap);
		}
		
		Set<String> mergedOutputColumns = outputColumnNamesByParameters.values().stream()
		        .reduce(new HashSet<>(), (all,perParams) -> { all.addAll(perParams); return all; });
		
		return new ContextColumnNameEncoder(requestToDefineToOutputColumn, mergedOutputColumns);
	}
	
	protected static Map<String, String> getDefineToOutputNameMap(CqlEvaluationRequest request, String defaultColumnDelimiter) {
		HashMap<String, String> defineToOutputNameMap = new HashMap<>();

		DefaultSparkOutputColumnEncoder encoder = new DefaultSparkOutputColumnEncoder(defaultColumnDelimiter);

		Set<String> outputColumns = new HashSet<>();

		CqlLibraryDescriptor descriptor = request.getDescriptor();
		if (descriptor == null || descriptor.getLibraryId() == null || descriptor.getLibraryId().isEmpty()) {
			throw new IllegalArgumentException("A library descriptor must be defined for each configured evaluation request.");
		}

		String libraryId = descriptor.getLibraryId();
		for (CqlExpressionConfiguration expression : request.getExpressions()) {
			String expressionName = expression.getName();
			String outputColumn = expression.getOutputColumn();

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
	
	private final Map<Integer, Map<String, String>> requestToDefineToOutputColumn;
	private final Set<String> outputColumnNames;
	
	private ContextColumnNameEncoder(Map<Integer, Map<String, String>> requestToDefineToOutputColumn, Set<String> outputColumnNames) {
		this.requestToDefineToOutputColumn = requestToDefineToOutputColumn;
		this.outputColumnNames = outputColumnNames;
	}
	
	public Set<String> getOutputColumnNames() {
		return outputColumnNames;
	}

	@Override
	public String getColumnName(CqlEvaluationRequest request, String defineName) {
		Map<String, String> defineToOutputName = requestToDefineToOutputColumn.get(request.getId());
		if (defineToOutputName == null) {
			throw new IllegalArgumentException("No lookup information found for CqlEvaluationRequest with id " + request.getId() 
													   + ". Did the id change since this object was created?");
		}
		return defineToOutputName.get(defineName);
	}
}
