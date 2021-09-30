package com.ibm.cohort.cql.spark;

import com.codahale.metrics.Gauge;

public class StringGauge implements Gauge<String> {
	private String value = "";

	public void setValue(String stringVal) {
		value = stringVal;
	}

	@Override
	public String getValue() {
		return value;
	}

}
