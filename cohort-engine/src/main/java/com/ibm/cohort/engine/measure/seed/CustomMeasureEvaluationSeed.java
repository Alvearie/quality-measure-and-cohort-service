package com.ibm.cohort.engine.measure.seed;

import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;

public class CustomMeasureEvaluationSeed implements IMeasureEvaluationSeed {

	private final Measure measure;
	private final Context context;
	private final Interval measurementPeriod;
	private final DataProvider dataProvider;

	public CustomMeasureEvaluationSeed(Measure measure, Context context, Interval measurementPeriod, DataProvider dataProvider) {
		this.measure = measure;
		this.context = context;
		this.measurementPeriod = measurementPeriod;
		this.dataProvider = dataProvider;
	}

	@Override
	public Measure getMeasure() {
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
