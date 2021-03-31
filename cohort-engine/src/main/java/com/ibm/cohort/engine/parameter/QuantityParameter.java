/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.opencds.cqf.cql.engine.runtime.Quantity;

public class QuantityParameter extends Parameter {
	@NotNull
	private String amount;
	@NotNull
	private String unit;
	
	public QuantityParameter() { 
		setType(ParameterType.QUANTITY);
	}
	
	public QuantityParameter(String amount, String unit) {
		this();
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
