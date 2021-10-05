package com.ibm.cohort.cql.evaluation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

//TODO: Use this or something similar to configure names
public class ExpressionConfiguration {
	private String expression;
	private String resultName;

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getResultName() {
		return resultName;
	}

	public void setResultName(String resultName) {
		this.resultName = resultName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		ExpressionConfiguration that = (ExpressionConfiguration) o;

		return new EqualsBuilder()
				.append(expression, that.expression)
				.append(resultName, that.resultName)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(expression)
				.append(resultName)
				.toHashCode();
	}
}
