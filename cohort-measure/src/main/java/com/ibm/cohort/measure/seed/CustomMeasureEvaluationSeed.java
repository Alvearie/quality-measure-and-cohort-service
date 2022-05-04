/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure.seed;

import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;

public class CustomMeasureEvaluationSeed<M> implements IMeasureEvaluationSeed<M> {

	private final M measure;
	private final Context context;
	private final Interval measurementPeriod;
	private final DataProvider dataProvider;

	public CustomMeasureEvaluationSeed(M measure, Context context, Interval measurementPeriod, DataProvider dataProvider) {
		this.measure = measure;
		this.context = context;
		this.measurementPeriod = measurementPeriod;
		this.dataProvider = dataProvider;
	}

	@Override
	public M getMeasure() {
		return measure;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public Interval getMeasurementPeriod() {
		return measurementPeriod;
	}

	@Override
	public DataProvider getDataProvider() {
		return dataProvider;
	}
}
