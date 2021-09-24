package com.ibm.cohort.cql.spark;

import org.apache.spark.util.LongAccumulator;

import com.codahale.metrics.Gauge;

public class LongAccumulatorGauge implements Gauge<Long> {
	private LongAccumulator longAc = null;

	public void setAccumulator(LongAccumulator longAccum) {
		longAc = longAccum;
	}

	@Override
	public Long getValue() {
		return longAc.value();
	}

}
