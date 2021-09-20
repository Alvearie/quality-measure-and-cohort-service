/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation.parameters;

import org.opencds.cqf.cql.engine.runtime.Date;

public class DateParameter extends StringBackedParameter {
	public DateParameter() {
		setType(ParameterType.DATE);
	}
	public DateParameter(String value) {
		this();
		setValue(value);
	}
	
	@Override
	public Object toCqlType() {
		return new Date(getValue());
	}
}
