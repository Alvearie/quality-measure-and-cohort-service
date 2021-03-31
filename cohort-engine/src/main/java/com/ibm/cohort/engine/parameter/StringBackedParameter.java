/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import javax.validation.constraints.NotNull;

public abstract class StringBackedParameter extends Parameter {
	@NotNull
	private String value;
	
	public StringBackedParameter() {
		setType(ParameterType.SIMPLE);
	}
	
	public StringBackedParameter(String value) {
		this();
		setValue(value);
	}
	
	public String getValue() {
		return value;
	}
	public Parameter setValue(String value) {
		this.value = value;
		return this;
	}
}
