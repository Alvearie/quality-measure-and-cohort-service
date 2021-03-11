package com.ibm.cohort.engine.api.service.model;

import java.time.OffsetDateTime;

import org.opencds.cqf.cql.engine.runtime.DateTime;

public class DatetimeParameter extends SimpleParameter {
	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZ";
	
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
		return new DateTime( OffsetDateTime.parse(getValue().replace("@","")) );
	}
}
