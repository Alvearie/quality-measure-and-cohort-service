/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import javax.validation.constraints.NotNull;

public class IntegerParameter extends Parameter {
	
	@NotNull
	private Integer value;
	
	public IntegerParameter() {
		setType(ParameterType.INTEGER);
	}
	
	public IntegerParameter(int value) { 
		this();
		setValue(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public Object toCqlType() {
		return value;
	}
}
