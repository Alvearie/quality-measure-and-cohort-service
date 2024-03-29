/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation.parameters;

import org.opencds.cqf.cql.engine.runtime.Time;

public class TimeParameter extends StringBackedParameter {
	public TimeParameter() {
		setType(ParameterType.TIME);
	}
	public TimeParameter(String value) {
		this();
		setValue(value);
	}
	
	@Override
	public Object toCqlType() {
		return new Time(getValue().replace("@", ""));
	}
}
