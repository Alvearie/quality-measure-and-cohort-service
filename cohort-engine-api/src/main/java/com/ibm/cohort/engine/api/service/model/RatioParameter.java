package com.ibm.cohort.engine.api.service.model;

import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;

public class RatioParameter extends Parameter {
	private QuantityParameter numerator;
	private QuantityParameter denominator;
	
	public RatioParameter() { 
		setType(ParameterType.RATIO);
	}
	
	public RatioParameter(String name, QuantityParameter numerator, QuantityParameter denominator) {
		this();
		setName(name);
		setNumerator( numerator );
		setDenominator( denominator );
	}
	
	public QuantityParameter getNumerator() {
		return numerator;
	}
	public RatioParameter setNumerator(QuantityParameter numerator) {
		this.numerator = numerator;
		return this;
	}
	
	public QuantityParameter getDenominator() {
		return denominator;
	}
	public RatioParameter setDenominator(QuantityParameter denominator) {
		this.denominator = denominator;
		return this;
	}
	
	@Override
	public Object toCqlType() {
		return new Ratio().setNumerator((Quantity)numerator.toCqlType()).setDenominator((Quantity)denominator.toCqlType());
	}
}
