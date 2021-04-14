/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import javax.validation.constraints.NotNull;

public class BooleanParameter extends Parameter {
	
	@NotNull
	private Boolean value;
	
	public BooleanParameter() {
		setType(ParameterType.BOOLEAN);
	}
	public BooleanParameter(boolean value) {
		this();
		setValue(value);
	}
	
	public boolean getValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
	
	@Override
	public Object toCqlType() {
		return value;
	}
}
