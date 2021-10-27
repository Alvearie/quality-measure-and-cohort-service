/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.errors;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class EvaluationSummary {
	private List<EvaluationError> errorList;
	
	public EvaluationSummary() {
		
	}
	
	public EvaluationSummary(List<EvaluationError> errorList) {
		this.errorList = errorList;
	}

	public List<EvaluationError> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<EvaluationError> errorList) {
		this.errorList = errorList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		EvaluationSummary that = (EvaluationSummary) o;

		return new EqualsBuilder()
				.append(errorList, that.errorList)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(errorList)
				.toHashCode();
	}
}
