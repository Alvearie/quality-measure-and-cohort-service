package com.ibm.cohort.engine.api.service.model;

import org.opencds.cqf.cql.engine.runtime.Date;

public class DateParameter extends SimpleParameter {
	public DateParameter() {
		setType(ParameterType.DATE);
	}
	public DateParameter(String name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	@Override
	public Object toCqlType() {
		return new Date(getValue());
	}
}
