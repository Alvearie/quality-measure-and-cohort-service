/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation.parameters;

public class StringParameter extends StringBackedParameter {
	public StringParameter() {
		setType(ParameterType.STRING);
	}
	
	public StringParameter(String value) {
		this();
		setValue(value);
	}
	
	@Override
	public Object toCqlType() {
		return getValue();
	}
}
