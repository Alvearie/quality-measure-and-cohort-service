package com.ibm.cohort.engine.api.service.model;

import java.math.BigDecimal;

public class DecimalParameter extends SimpleParameter {
	public DecimalParameter() {
		setType(ParameterType.DECIMAL);
	}
	public DecimalParameter(String name, String value) {
		this();
		setName( name );
		setValue( value );
	}
	
	@Override
	public Object toCqlType() {
		return new BigDecimal(getValue());
	}
}
