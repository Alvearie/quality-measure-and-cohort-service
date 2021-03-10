package com.ibm.cohort.engine.api.service.model;

public class StringParameter extends SimpleParameter {
	public StringParameter() {
		setType(ParameterType.STRING);
	}
	
	public StringParameter(String name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	@Override
	public Object toCqlType() {
		return getValue();
	}
}
