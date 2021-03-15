/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import java.math.BigDecimal;

public class DecimalParameter extends SimpleParameter {
	public DecimalParameter() {
		setType(ParameterType.DECIMAL);
	}
	public DecimalParameter(String value) {
		this();
		setValue( value );
	}
	
	@Override
	public Object toCqlType() {
		return new BigDecimal(getValue());
	}
}
