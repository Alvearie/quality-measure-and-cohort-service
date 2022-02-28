/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Measure;

import com.ibm.cohort.cql.evaluation.parameters.Parameter;

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
	 * @param measure FHIR Measure resource
	 * @param parameterOverrides parameter values used to override resource defaults
	 * @return measurement period as strings
	 */
	public Pair<String,String> getMeasurementPeriod(Measure measure, Map<String,Parameter> parameterOverrides);
}
