package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;


public class ConfigurableOutputColumnNameEncoder implements SparkOutputColumnEncoder, Serializable {
	public static ConfigurableOutputColumnNameEncoder create(CqlEvaluationRequests evaluationRequests, String defaultNameDelimiter) {
		Map<String, List<CqlEvaluationRequest>> requestsByContext = new HashMap<>();
		
		// Gather requests by context
		for (CqlEvaluationRequest request : evaluationRequests.getEvaluations()) {
			String contextKey = request.getContextKey();
			List<CqlEvaluationRequest> contextRequests = requestsByContext.computeIfAbsent(contextKey, x -> new ArrayList<>());
			contextRequests.add(request);
		}

		Map<String, ContextColumnNameEncoder> contextKeyToContextNameEncoder = new HashMap<>();
		Set<String> outputColumnNames = new HashSet<>();

		for (String contextKey : requestsByContext.keySet()) {
			ContextColumnNameEncoder nameEncoder = ContextColumnNameEncoder.create(requestsByContext.get(contextKey), defaultNameDelimiter);
			contextKeyToContextNameEncoder.put(contextKey, nameEncoder);

			for (String outputColumnName : nameEncoder.getOutputColumnNames()) {
				if (outputColumnNames.contains(outputColumnName)) {
					throw new IllegalArgumentException("Output column " + outputColumnName + " defined multiple times.");
				}
				outputColumnNames.add(outputColumnName);
			}
			
		}

		
		return new ConfigurableOutputColumnNameEncoder(contextKeyToContextNameEncoder);
	}

	private final Map<String, ContextColumnNameEncoder> contextKeyToContextColumnNameEncoder;
	
	private ConfigurableOutputColumnNameEncoder(Map<String, ContextColumnNameEncoder> contextKeyToContextColumnNameEncoder) {
		this.contextKeyToContextColumnNameEncoder = contextKeyToContextColumnNameEncoder;
	}

	@Override
	public String getColumnName(CqlEvaluationRequest request, String defineName) {
		ContextColumnNameEncoder contextColumnNameEncoder = contextKeyToContextColumnNameEncoder.get(request.getContextKey());
		return contextColumnNameEncoder.getColumnName(request, defineName);
	}
}
