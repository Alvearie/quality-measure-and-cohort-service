package com.ibm.cohort.engine.api.service.model;

public class BooleanParameter extends Parameter {
	private boolean value;
	
	public BooleanParameter() {
		setType(ParameterType.BOOLEAN);
	}
	public BooleanParameter(String name, boolean value) {
		this();
		setName(name);
		setValue(value);
	}
	
	public boolean isValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
	
	@Override
	public Object toCqlType() {
		return Boolean.valueOf(value);
	}
}
