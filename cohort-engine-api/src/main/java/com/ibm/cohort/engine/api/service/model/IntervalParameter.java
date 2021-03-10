package com.ibm.cohort.engine.api.service.model;

import org.opencds.cqf.cql.engine.runtime.Interval;

public class IntervalParameter extends Parameter {
	private Parameter start;
	private boolean startInclusive;
	private Parameter end;	
	private boolean endInclusive;
	
	public IntervalParameter() { 
		setType(ParameterType.INTERVAL);
	}
	
	public IntervalParameter( String name, Parameter start, boolean startInclusive, Parameter end, boolean endInclusive ) {
		this();
		setName(name);
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

	public void setStartInclusive(boolean startInclusive) {
		this.startInclusive = startInclusive;
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

	public void setEndInclusive(boolean endInclusive) {
		this.endInclusive = endInclusive;
	}
	
	@Override
	public Object toCqlType() {
		return new Interval( start.toCqlType(), startInclusive, end.toCqlType(), endInclusive );
	}

}
