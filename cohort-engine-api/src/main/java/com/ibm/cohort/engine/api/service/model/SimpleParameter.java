package com.ibm.cohort.engine.api.service.model;

public abstract class SimpleParameter extends Parameter {
	private String value;
	
	public SimpleParameter() {
		setType(ParameterType.SIMPLE);
	}
	
	public SimpleParameter(String name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	public String getValue() {
		return value;
	}
	public Parameter setValue(String value) {
		this.value = value;
		return this;
	}
}
