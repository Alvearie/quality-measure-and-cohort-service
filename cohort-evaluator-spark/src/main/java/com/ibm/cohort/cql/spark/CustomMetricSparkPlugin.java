package com.ibm.cohort.cql.spark;

import java.util.Map;

import org.apache.spark.api.plugin.DriverPlugin;
import org.apache.spark.api.plugin.ExecutorPlugin;
import org.apache.spark.api.plugin.PluginContext;
import org.apache.spark.api.plugin.SparkPlugin;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

public class CustomMetricSparkPlugin implements SparkPlugin{

//	public static final Gauge inProgressEvaluations;
	public static Counter dataRowsProcessed;
	
	@Override
	public DriverPlugin driverPlugin() {
		return new DriverPlugin() {
			public void registerMetrics(String appId, PluginContext pluginContext) {
				MetricRegistry metReg = pluginContext.metricRegistry();
				
			}
		};
	}

	@Override
	public ExecutorPlugin executorPlugin() {
		return new ExecutorPlugin() {
			public void init(PluginContext ctx, Map<String, String> extraConf) {
				MetricRegistry metReg = ctx.metricRegistry();
				dataRowsProcessed = metReg.counter("metrics_dataRowsProcessed");
				
			}
		};
//		return ExecutorPlugin{
//			void init(init(ctx, extraConf){
//				
//			}
//		}
	}

//  driverPlugin():
//	  \DriverPlugin = null
//  override def executorPlugin(): ExecutorPlugin = new ExecutorPlugin {
//   override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
//      val metricRegistry = ctx.metricRegistry()
//      metricRegistry.register("evenMetrics",CustomMetricSparkPlugin.value)
//    }
//  }
}