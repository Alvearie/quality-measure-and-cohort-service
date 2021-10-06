/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metrics;

import org.apache.spark.util.LongAccumulator;

import com.codahale.metrics.Gauge;

public class LongAccumulatorGauge implements Gauge<Long> {
	private LongAccumulator longAc = null;

	public void setAccumulator(LongAccumulator longAccum) {
		longAc = longAccum;
	}

	@Override
	public Long getValue() {
		if (longAc != null) {
			return longAc.value();
		} else {
			return new Long(0);
		}
	}

}
