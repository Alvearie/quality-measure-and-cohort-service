package com.ibm.cohort.engine.measure;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Provide the ability for user's to customize the logic used
 * to determine the measurement period. User's may also just
 * call the {@link MeasureEvaluator} with periodStart and
 * periodEnd directly if they so wish.
 */
public interface MeasurementPeriodStrategy {
	/**
	 * Get a Pair of string values that represent the measurement
	 * period. The left side is the start and the right side of 
	 * the pair is the end.
	 * @return measurement period as strings
	 */
	public Pair<String,String> getMeasurementPeriod();
}
