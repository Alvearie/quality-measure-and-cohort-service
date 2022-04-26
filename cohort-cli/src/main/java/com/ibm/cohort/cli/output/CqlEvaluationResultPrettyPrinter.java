/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli.output;

import java.util.Collection;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IAnyResource;

import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;

public abstract class CqlEvaluationResultPrettyPrinter {
	public final String prettyPrintResult(CqlEvaluationResult result) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String,Object> entry : result.getExpressionResults().entrySet()) {
			String expression = entry.getKey();
			Object value = entry.getValue();

			builder.append("Expression: \"").append(expression).append("\", ");
			builder.append("Result: ").append(prettyPrintValue(value)).append('\n');
		}
		return builder.toString();
	}
	
	protected final String prettyPrintValue(Object value) {
		return prettyPrintValue(new StringBuilder(), value).toString();
	}
	
	protected abstract StringBuilder handleCollection(Object value);

	protected final StringBuilder prettyPrintValue(StringBuilder sb, Object value) {
		if( value != null ) {
			if( value instanceof IAnyResource) {
				IAnyResource resource = (IAnyResource) value;
				sb.append(resource.getId());
			} else if( value instanceof Collection) {
				sb.append(handleCollection(value));
			} else {
				sb.append(value);
			}
		} else {
			sb.append("null");
		}
		return sb;
	}
}
