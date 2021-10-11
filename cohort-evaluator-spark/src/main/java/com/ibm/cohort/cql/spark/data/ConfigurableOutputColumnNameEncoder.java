package com.ibm.cohort.cql.spark.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;

/**
 * Goal of this class is to parse CqlEvaluationRequests and calculate the output
 * column name for each define that will eventually be run by the Spark engine.
 * 
 * The primary interface to this class takes a CqlEvaluationRequest and a
 * define name and returns the appropriate output column name.
 * 
 * As output columns are being calculated, various error checks are performed
 * to make sure that a pair of CqlEvaluationRequest and define name objects
 * resolve to a single output column name. There is also error checking in
 * place to make sure output column names are unique for every context
 * referenced in the CqlEvaluationRequests object's list of requests.
 */
public class ConfigurableOutputColumnNameEncoder implements SparkOutputColumnEncoder {
	public static ConfigurableOutputColumnNameEncoder create(CqlEvaluationRequests evaluationRequests, String defaultNameDelimiter) {
		Map<String, List<CqlEvaluationRequest>> requestsByContext = new HashMap<>();
		
		// Gather requests by context
		for (CqlEvaluationRequest request : evaluationRequests.getEvaluations()) {
			String contextKey = request.getContextKey();
			List<CqlEvaluationRequest> contextRequests = requestsByContext.computeIfAbsent(contextKey, x -> new ArrayList<>());
			contextRequests.add(request);
		}

		Map<String, ContextColumnNameEncoder> contextKeyToContextNameEncoder = new HashMap<>();

		for (String contextKey : requestsByContext.keySet()) {
			ContextColumnNameEncoder nameEncoder = ContextColumnNameEncoder.create(requestsByContext.get(contextKey), defaultNameDelimiter);
			contextKeyToContextNameEncoder.put(contextKey, nameEncoder);
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
