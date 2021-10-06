/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metrics;

import com.codahale.metrics.Gauge;

public class IntGauge implements Gauge<Integer> {
	private int value = -1;

	public void setValue(int intVal) {
		value = intVal;
	}

	@Override
	public Integer getValue() {
		return value;
	}

}
