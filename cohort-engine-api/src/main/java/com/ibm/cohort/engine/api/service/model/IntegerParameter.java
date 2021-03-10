package com.ibm.cohort.engine.api.service.model;

public class IntegerParameter extends Parameter {
	private int value;
	
	public IntegerParameter() {
		setType(ParameterType.INTEGER);
	}
	
	public IntegerParameter(String name, int value) { 
		this();
		setName(name);
		setValue(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public Object toCqlType() {
		return Integer.valueOf(value);
	}
}
