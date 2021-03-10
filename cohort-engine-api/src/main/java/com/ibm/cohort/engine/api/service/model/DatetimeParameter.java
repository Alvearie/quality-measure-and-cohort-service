package com.ibm.cohort.engine.api.service.model;

import java.time.OffsetDateTime;

import org.opencds.cqf.cql.engine.runtime.DateTime;

public class DatetimeParameter extends SimpleParameter {
	public DatetimeParameter() {
		setType(ParameterType.DATETIME);
	}
	public DatetimeParameter(String name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	@Override
	public Object toCqlType() {
		return new DateTime(getValue().replace("@", ""), OffsetDateTime.now().getOffset());
	}
}
