package com.ibm.cohort.cql.spark.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;

public class ContextColumnNamer {
	public static ContextColumnNamer create(List<CqlEvaluationRequest> contextRequests, String defaultColumnDelimiter) {
		Map<CqlEvaluationRequest, Map<String, String>> requestToDefineToOutputColumn = new HashMap<>();
		Set<String> outputColumnNames = new HashSet<>();
		
		for (CqlEvaluationRequest contextRequest : contextRequests) {
			Map<String, String> defineToOutputNameMap = CqlEvaluationRequestNamer.getDefineToOutputNameMap(contextRequest, defaultColumnDelimiter);
			
			// Make sure output names are unique across requests
			for (String value : defineToOutputNameMap.values()) {
				if (outputColumnNames.contains(value)) {
					throw new IllegalArgumentException("Duplicate outputColumn " + value + " defined in the job definition file.");
				}
				outputColumnNames.add(value);
			}

			requestToDefineToOutputColumn.put(contextRequest, defineToOutputNameMap);
		}
		
		return new ContextColumnNamer(requestToDefineToOutputColumn, outputColumnNames);
	}
	
	private final Map<CqlEvaluationRequest, Map<String, String>> requestToDefineToOutputColumn;
	private final Set<String> outputColumnNames;
	
	private ContextColumnNamer(Map<CqlEvaluationRequest, Map<String, String>> requestToDefineToOutputColumn, Set<String> outputColumnNames) {
		this.requestToDefineToOutputColumn = requestToDefineToOutputColumn;
		this.outputColumnNames = outputColumnNames;
	}
	
	public String getOutputColumn(CqlEvaluationRequest request, String defineName) {
		Map<String, String> defineToOutputName = requestToDefineToOutputColumn.get(request);
		if (defineToOutputName == null) {
			throw new IllegalArgumentException("Cannot find column name data for the provided request.");
		}
		return defineToOutputName.get(defineName);
	}
	
	public Set<String> getOutputColumnNames() {
		return outputColumnNames;
	}
}
