/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

public abstract class SimpleParameter extends Parameter {
	private String value;
	
	public SimpleParameter() {
		setType(ParameterType.SIMPLE);
	}
	
	public SimpleParameter(String value) {
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
