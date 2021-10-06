package com.ibm.cohort.cql.spark.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;

public class OutputColumnNamer {
	public static OutputColumnNamer create(CqlEvaluationRequests evaluationRequests, String defaultNameDelimiter) {
		Map<String, List<CqlEvaluationRequest>> requestsByContext = new HashMap<>();
		
		// Gather requests by context
		for (CqlEvaluationRequest request : evaluationRequests.getEvaluations()) {
			String contextKey = request.getContextKey();
			List<CqlEvaluationRequest> contextRequests = requestsByContext.computeIfAbsent(contextKey, x -> new ArrayList<>());
			contextRequests.add(request);
		}

		Map<String, ContextColumnNamer> contextKeyToContextNamer = new HashMap<>();
		Set<String> outputColumnNames = new HashSet<>();
		// For each context, calculate the names and such
		for (String contextKey : requestsByContext.keySet()) {
			ContextColumnNamer namer = ContextColumnNamer.create(requestsByContext.get(contextKey), defaultNameDelimiter);
			contextKeyToContextNamer.put(contextKey, namer);

			for (String outputColumnName : namer.getOutputColumnNames()) {
				if (outputColumnNames.contains(outputColumnName)) {
					throw new IllegalArgumentException("Output column " + outputColumnName + " defined multiple times.");
				}
				outputColumnNames.add(outputColumnName);
			}
			
		}

		
		return new OutputColumnNamer(contextKeyToContextNamer);
	}

	private final Map<String, ContextColumnNamer> contextKeyToContextNamer;
	
	private OutputColumnNamer(Map<String, ContextColumnNamer> contextKeyToContextNamer) {
		this.contextKeyToContextNamer = contextKeyToContextNamer;
	}
	
	public String getOutputColumn(CqlEvaluationRequest request, String defineExpression) {
		ContextColumnNamer contextColumnNamer = contextKeyToContextNamer.get(request.getContextKey());
		return contextColumnNamer.getOutputColumn(request, defineExpression);
	}
}
