/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Measure;

import com.ibm.cohort.engine.parameter.DateParameter;
import com.ibm.cohort.engine.parameter.DatetimeParameter;
import com.ibm.cohort.engine.parameter.IntervalParameter;
import com.ibm.cohort.engine.parameter.Parameter;

/**
 * Provide a very basic implementation of a measurement period determination
 * strategy. This is used to inject different behaviors into the
 * MeasurementEvaluator and only when the caller does not want to provide the
 * periodStart and periodEnd directly. Callers should plan on customizing this
 * as needed for their use case.
 * 
 * The most simple use is where the period is defined as the current datetime in
 * the system timezone adjusted by by minus one year. Users can customize this
 * behavior by initializing the strategy with an <code>amount</code> and
 * <code>unit</code> where the unit comes from {@link java.util.Calendar}.
 * 
 * Users may provide a fixed date for "now" for cases where they want this
 * strategy to always return the same period across multiple invocations.
 */
public class DefaultMeasurementPeriodStrategy implements MeasurementPeriodStrategy {

	public static final String DEFAULT_MEASUREMENT_PERIOD_PARAMETER = "Measurement Period";
	public static final String TARGET_DATE_FORMAT = "yyyy-MM-dd";

	private static final int DEFAULT_AMOUNT = -1;
	private static final int DEFAULT_UNIT = Calendar.YEAR;

	private String measurementPeriodParameter = DEFAULT_MEASUREMENT_PERIOD_PARAMETER;
	private int unit;
	private int amount;

	private Date now;

	/**
	 * Use a measurement strategy with the default logic.
	 */
	public DefaultMeasurementPeriodStrategy() {
		this(DEFAULT_UNIT, DEFAULT_AMOUNT);
	}

	/**
	 * Use a measurement strategy where the duration of the period is determined by
	 * the user.
	 * 
	 * @param unit   Date part constant from {@link java.util.Calendar}.
	 * @param amount Amount of <code>unit</code> that should be added to current
	 *               system datetime to create the measurement period.
	 */
	public DefaultMeasurementPeriodStrategy(int unit, int amount) {
		this.unit = unit;
		this.amount = amount;
	}
	
	/**
	 * Set the parameter name of the measurement period parameter. This is
	 * optional and will default to <code>DEFAULT_MEASUREMENT_PERIOD_PARAMETER</code>
	 * if not provided;
	 * 
	 * @param name parameter name
	 * @return this
	 */
	public DefaultMeasurementPeriodStrategy setMeasurementPeriodParameter(String name) {
		this.measurementPeriodParameter = name;
		return this;
	}
	
	/**
	 * Get the parameter name of the measurement period parameter.
	 * @return parameter name
	 */
	public String getMeasurementPeriodParameter() {
		return this.measurementPeriodParameter;
	}

	/**
	 * Allow the user to provide the "now" date as needed.
	 * 
	 * @param date the "now" date for the system timezone.
	 * @return this
	 */
	public DefaultMeasurementPeriodStrategy setNow(Date date) {
		this.now = date;
		return this;
	}

	/**
	 * Return the now date for this instance. If the user has not specified a fixed
	 * "now" date, then each time this is invoked the current system time is
	 * returned.
	 * 
	 * @return current system time or the fixed now date if set.
	 */
	public Date getNow() {
		Date date = this.now;
		if (date == null) {
			date = new Date();
		}
		return date;
	}

	@Override
	public Pair<String, String> getMeasurementPeriod(Measure measure, Map<String,Parameter> parameterOverrides) {
		Pair<String,String> result = null;
		
		if( parameterOverrides != null ) { 
			Object measurementPeriod = parameterOverrides.get(measurementPeriodParameter);
			if( measurementPeriod != null ) {
				result = intervalToPair((IntervalParameter)measurementPeriod);
			}
		}
		
		if( result == null ) {
			result = calculateMeasurementPeriod();
		}
		return result;
	}

	protected Pair<String, String> intervalToPair(IntervalParameter interval) {
		String start = null;
		String end = null; 
		
		if( interval.getStart() instanceof DatetimeParameter ) {
			DatetimeParameter dtStart = (DatetimeParameter) interval.getStart();
			start = dtStart.getValue();
			
			DatetimeParameter dtEnd = (DatetimeParameter) interval.getEnd();
			end = dtEnd.getValue();
		} else if( interval.getStart() instanceof DateParameter ) {
			DateParameter dStart = (DateParameter) interval.getStart();
			start = dStart.getValue();
			
			DateParameter dEnd = (DateParameter) interval.getEnd();
			end = dEnd.getValue();
		} else {
			throw new IllegalArgumentException(String.format("Unexpected interval data type '%s'", interval.getStart().getClass()));
		}
		
		return new ImmutablePair<String,String>( start, end );
	}

	protected Pair<String, String> calculateMeasurementPeriod() {
		Calendar end = Calendar.getInstance();
		end.setTime(getNow());

		Calendar start = Calendar.getInstance();
		start.setTime(end.getTime());
		start.add(unit, amount);

		if (end.getTime().before(start.getTime())) {
			Calendar tmp = end;
			end = start;
			start = tmp;
		}

		DateFormat sdf = new SimpleDateFormat(TARGET_DATE_FORMAT);
		String periodStart = sdf.format(start.getTime());
		String periodEnd = sdf.format(end.getTime());

		return new ImmutablePair<String, String>(periodStart, periodEnd);
	}
}
