/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.metrics;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ibm.cohort.cql.spark.metrics.CustomMetricSparkPlugin;

public class CustomSparkMetricTest {

	@Test
	public void testCustomMetricSparkPlugin() {
		CustomMetricSparkPlugin plug = new CustomMetricSparkPlugin();
		assertNotNull(plug.driverPlugin());
		assertNotNull(plug.executorPlugin());

	}
}
