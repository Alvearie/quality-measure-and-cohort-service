package com.ibm.cohort.cql.spark.data;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;

public class OutputColumnNamer {
	public static OutputColumnNamer create(CqlEvaluationRequests evaluationRequests, String defaultNameDelimiter) {

		// TODO: Figure out output names per context.
		//       Throw error if names repeat. (Maybe auto-increment at some point?)

		// Gather requests by context

		// For each context, calculate the names and such

		// Initialize a lookup table that returns the output column name for a definition
		// Can index by context or by cql eval request
		return new OutputColumnNamer();
	}
	
}
