/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CqlExpressionConfigurationDeserializer.class)
public class CqlExpressionConfiguration {
	private String name;
	private String outputColumn;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getoutputColumn() {
		return outputColumn;
	}

	public void setoutputColumn(String outputColumn) {
		this.outputColumn = outputColumn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		CqlExpressionConfiguration that = (CqlExpressionConfiguration) o;

		return new EqualsBuilder()
				.append(name, that.name)
				.append(outputColumn, that.outputColumn)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(name)
				.append(outputColumn)
				.toHashCode();
	}
}
