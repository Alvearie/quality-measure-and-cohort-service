/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import javax.validation.constraints.NotNull;
import org.opencds.cqf.cql.engine.runtime.Interval;

public class IntervalParameter extends Parameter {
	@NotNull
	private Parameter start;
	private boolean startInclusive;
	@NotNull
	private Parameter end;	
	private boolean endInclusive;
	
	public IntervalParameter() { 
		setType(ParameterType.INTERVAL);
	}
	
	public IntervalParameter( Parameter start, boolean startInclusive, Parameter end, boolean endInclusive ) {
		this();
		setStart(start);
		setStartInclusive(startInclusive);
		setEnd(end);
		setEndInclusive(endInclusive);
	}
	
	public Parameter getStart() {
		return start;
	}
	public IntervalParameter setStart(Parameter start) {
		this.start = start;
		return this;
	}

	public boolean isStartInclusive() {
		return startInclusive;
	}
	public IntervalParameter setStartInclusive(boolean startInclusive) {
		this.startInclusive = startInclusive;
		return this;
	}
	
	public Parameter getEnd() {
		return end;
	}
	public IntervalParameter setEnd(Parameter end) {
		this.end = end;
		return this;
	}

	public boolean isEndInclusive() {
		return endInclusive;
	}

	public IntervalParameter setEndInclusive(boolean endInclusive) {
		this.endInclusive = endInclusive;
		return this;
	}
	
	@Override
	public Object toCqlType() {
		return new Interval( start.toCqlType(), startInclusive, end.toCqlType(), endInclusive );
	}
}
