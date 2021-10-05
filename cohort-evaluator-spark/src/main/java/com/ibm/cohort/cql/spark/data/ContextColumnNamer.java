package com.ibm.cohort.cql.spark.data;

import java.util.List;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;

public class ContextColumnNamer {
	// assume a single context of cqlevaluationrequests
	
	public static ContextColumnNamer create(List<CqlEvaluationRequest> contextRequests, String defaultColumnDelimiter) {
		// TODO: Figure out how to name things for a single context.
		//       Easiest is to allow overrides and default naming. Blow up if duplicates exist.
		//       It might be possible to auto-increment a column name, but then how do we tie it to the original request?

		return new ContextColumnNamer();
	}
	
	private ContextColumnNamer() {
		
	}
}
