/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;

public class RatioParameter extends Parameter {
	@NotNull
	@Valid
	private QuantityParameter numerator;
	@NotNull
	@Valid
	private QuantityParameter denominator;
	
	public RatioParameter() { 
		setType(ParameterType.RATIO);
	}
	
	public RatioParameter(QuantityParameter numerator, QuantityParameter denominator) {
		this();
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
