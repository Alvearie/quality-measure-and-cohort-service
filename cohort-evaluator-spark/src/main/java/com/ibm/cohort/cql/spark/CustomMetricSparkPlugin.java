package com.ibm.cohort.cql.spark;

import org.apache.spark.api.plugin.DriverPlugin;
import org.apache.spark.api.plugin.ExecutorPlugin;
import org.apache.spark.api.plugin.PluginContext;
import org.apache.spark.api.plugin.SparkPlugin;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

public class CustomMetricSparkPlugin implements SparkPlugin{

//	public static final Gauge inProgressEvaluations;
	//public static Counter dataRowsProcessed = new MetricRegistry().counter("metrics_dataRowsProcessed");
	//public static Counter driverDataRowsProcessed = new MetricRegistry().counter("metrics_driverDataRowsProcessed");
	
	public static LongAccumulatorGauge contextAccumMetric = new LongAccumulatorGauge();	
	public static LongAccumulatorGauge perContextAccumMetric = new LongAccumulatorGauge();
	public static Counter contextUnionsCounter = new Counter();
	//public static IntGauge contextUnions = new IntGauge();
	
	public static Counter cohortMetricCounter = new Counter();

	
	@Override
	public DriverPlugin driverPlugin() {
		return new DriverPlugin() {
			@Override
			public void registerMetrics(String appId, PluginContext pluginContext) {
				MetricRegistry metReg = pluginContext.metricRegistry();
				metReg.register(MetricRegistry.name("metrics_CohortContextAccum"), contextAccumMetric);
				metReg.register(MetricRegistry.name("metrics_Cohort_perContextAccum"), perContextAccumMetric);
				metReg.register(MetricRegistry.name("metrics_CohortMetricCounter"), cohortMetricCounter);
				metReg.register(MetricRegistry.name("metrics_CohortContextUnionsCounter"), contextUnionsCounter);
				
			}
		};
	}

	@Override
	public ExecutorPlugin executorPlugin() {
		return new ExecutorPlugin() {
//			@Override
//			public void init(PluginContext ctx, Map<String, String> extraConf) {
//				MetricRegistry metReg = ctx.metricRegistry();
//				metReg.register(MetricRegistry.name("metrics_dataRowsProcessed"), dataRowsProcessed);
//				
//			}
		};
	}
}