package com.ibm.cohort.engine.api.service.model;

import java.math.BigDecimal;

import org.opencds.cqf.cql.engine.runtime.Quantity;

public class QuantityParameter extends Parameter {
	private String amount;
	private String unit;
	
	public QuantityParameter() { 
		setType(ParameterType.QUANTITY);
	}
	
	public QuantityParameter(String name, String amount, String unit) {
		this();
		setName(name);
		setAmount(amount);
		setUnit(unit);
	}
	
	public String getAmount() {
		return amount;
	}
	public QuantityParameter setAmount(String amount) {
		this.amount = amount;
		return this;
	}
	public String getUnit() {
		return unit;
	}
	public QuantityParameter setUnit(String unit) {
		this.unit = unit;
		return this;
	}	
	
	@Override
	public Object toCqlType() {
		return new Quantity().withUnit(unit).withValue(new BigDecimal(amount));
	}
}
