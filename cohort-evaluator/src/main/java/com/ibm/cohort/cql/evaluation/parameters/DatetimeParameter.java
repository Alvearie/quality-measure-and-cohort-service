/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation.parameters;

import java.time.ZoneOffset;

import org.opencds.cqf.cql.engine.runtime.DateTime;

public class DatetimeParameter extends StringBackedParameter {
	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZ";
	
	public DatetimeParameter() {
		setType(ParameterType.DATETIME);
	}
	public DatetimeParameter(String value) {
		this();
		setValue(value);
	}
	
	@Override
	public Object toCqlType() {
		return new DateTime(getValue().replace("@",""), ZoneOffset.UTC);
	}
}
