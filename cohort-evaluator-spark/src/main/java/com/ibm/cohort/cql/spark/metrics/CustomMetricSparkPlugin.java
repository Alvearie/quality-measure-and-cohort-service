/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metrics;

import org.apache.spark.api.plugin.DriverPlugin;
import org.apache.spark.api.plugin.ExecutorPlugin;
import org.apache.spark.api.plugin.PluginContext;
import org.apache.spark.api.plugin.SparkPlugin;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

public class CustomMetricSparkPlugin implements SparkPlugin{
	
	public static LongAccumulatorGauge contextAccumGauge= new LongAccumulatorGauge();	
	public static IntGauge currentlyEvaluatingContextGauge = new IntGauge();	
	public static LongAccumulatorGauge perContextAccumGauge = new LongAccumulatorGauge();
	public static Counter contextUnionsCompletedCounter = new Counter();
	public static Counter totalContextsToProcessCounter = new Counter();

	
	@Override
	public DriverPlugin driverPlugin() {
		return new DriverPlugin() {
			@Override
			public void registerMetrics(String appId, PluginContext pluginContext) {
				MetricRegistry metReg = pluginContext.metricRegistry();
				//Tracks which context if currently being processed
				metReg.register(MetricRegistry.name("Cohort_ContextAccum"), contextAccumGauge);
				//Tracks cql evaluations per context, resetting after each context finishes
				metReg.register(MetricRegistry.name("Cohort_PerContextAccum"), perContextAccumGauge);
				//The total number of contexts there are to process
				metReg.register(MetricRegistry.name("Cohort_TotalContextsToProcessCounter"), totalContextsToProcessCounter);
				//Which context is currently being processed represented as a number (ie 1, 2, 3, 4)
				metReg.register(MetricRegistry.name("Cohort_CurrentlyEvaluatingContext"), currentlyEvaluatingContextGauge);
				
			}
		};
	}

	@Override
	public ExecutorPlugin executorPlugin() {
		return new ExecutorPlugin() {
			//Executor metrics are not automatically exposed via the prometheus servlet like the driver metrics are
			//Keeping this here as an example in case they fix/add support for this in future releases
//			@Override
//			public void init(PluginContext ctx, Map<String, String> extraConf) {
//				MetricRegistry metReg = ctx.metricRegistry();
//				metReg.register(MetricRegistry.name("metrics_dataRowsProcessed"), dataRowsProcessed);
//				
//			}
		};
	}
}