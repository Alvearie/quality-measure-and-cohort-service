package com.ibm.cohort.cql.spark;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CustomSparkMetricTest {

    @Test
    public void testCustomMetricSparkPlugin() {
    	CustomMetricSparkPlugin plug = new CustomMetricSparkPlugin();
    	assertNotNull(plug.driverPlugin());
    	assertNotNull(plug.executorPlugin());
    	
    }
}
