package com.ibm.cohort.engine.api.service.model;

import org.opencds.cqf.cql.engine.runtime.Time;

public class TimeParameter extends SimpleParameter {
	public TimeParameter() {
		setType(ParameterType.TIME);
	}
	public TimeParameter(String name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	@Override
	public Object toCqlType() {
		return new Time(getValue().replace("@", ""));
	}
}
