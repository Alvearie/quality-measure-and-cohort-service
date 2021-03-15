/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import org.opencds.cqf.cql.engine.runtime.Time;

public class TimeParameter extends SimpleParameter {
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
