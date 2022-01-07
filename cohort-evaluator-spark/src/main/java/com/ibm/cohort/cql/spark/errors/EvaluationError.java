/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.errors;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EvaluationError implements Serializable {
	private static final long serialVersionUID = -1300727992961144423L;
	
	private String contextName;
	private Object contextId;
	private String outputColumn;
	private String exception;
	
	public EvaluationError() {
		
	}

	public EvaluationError(String contextName, Object contextId, String outputColumn, String exception) {
		this.contextName = contextName;
		this.contextId = contextId;
		this.outputColumn = outputColumn;
		this.exception = exception;
	}

	public String getContextName() {
		return contextName;
	}

	public Object getContextId() {
		return contextId;
	}

	public String getOutputColumn() {
		return outputColumn;
	}

	public String getException() {
		return exception;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		EvaluationError that = (EvaluationError) o;

		return new EqualsBuilder()
				.append(contextName, that.contextName)
				.append(contextId, that.contextId)
				.append(outputColumn, that.outputColumn)
				.append(exception, that.exception)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(contextName)
				.append(contextId)
				.append(outputColumn)
				.append(exception)
				.toHashCode();
	}
}
