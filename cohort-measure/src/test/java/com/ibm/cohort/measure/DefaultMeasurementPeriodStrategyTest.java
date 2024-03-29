/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Test;

import com.ibm.cohort.cql.evaluation.parameters.DateParameter;
import com.ibm.cohort.cql.evaluation.parameters.DatetimeParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntervalParameter;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;

public class DefaultMeasurementPeriodStrategyTest {

	@Test
	public void default_values__start_before_end() throws Exception {
		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy();
		runCalculateMeasurementPeriodTest(strategy);
	}

	@Test
	public void positive_amount__start_before_end() throws Exception {
		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy(Calendar.MONTH, 1);
		runCalculateMeasurementPeriodTest(strategy);
	}

	@Test
	public void negative_amount__start_before_end() throws Exception {
		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy(Calendar.MONTH, -1);
		runCalculateMeasurementPeriodTest(strategy);
	}

	@Test
	public void fixed_now_negative_amount__is_correct_end() throws Exception {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2020, Calendar.OCTOBER, 20);

		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy(Calendar.MONTH, -1);
		strategy.setNow(c.getTime());

		Pair<String, String> period = runCalculateMeasurementPeriodTest(strategy);

		SimpleDateFormat sdf = getDateFormat();
		String expected = sdf.format(c.getTime());
		assertEquals(expected, period.getRight());
	}

	@Test
	public void fixed_now_positive_amount__is_correct_start() throws Exception {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2020, Calendar.OCTOBER, 20);

		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy(Calendar.MONTH, 1);
		strategy.setNow(c.getTime());

		Pair<String, String> period = runCalculateMeasurementPeriodTest(strategy);

		SimpleDateFormat sdf = getDateFormat();
		String expected = sdf.format(c.getTime());
		assertEquals(expected, period.getLeft());
	}

	@Test
	public void parameter_not_null___parameter_is_used() throws Exception {
		runGetMeasurementPeriodTest(DefaultMeasurementPeriodStrategy.DEFAULT_MEASUREMENT_PERIOD_PARAMETER, "1900-01-01",
				"2000-01-01");
	}

	@Test
	public void parameter_name_overridden_value_not_null___parameter_is_used() throws Exception {
		runGetMeasurementPeriodTest("My Measurement Period", "2020-02-11", "2021-02-11");
	}
	
	@Test
	public void paramter_null___value_is_calulated() throws Exception {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2020, Calendar.OCTOBER, 20);
		
		Measure measure = new Measure();
		Map<String, Parameter> parameterOverrides = Collections.emptyMap();

		Pair<String, String> result = new DefaultMeasurementPeriodStrategy()
				.setNow(c.getTime()).getMeasurementPeriod(measure, parameterOverrides);
		
		DateFormat sdf = new SimpleDateFormat(DefaultMeasurementPeriodStrategy.TARGET_DATE_FORMAT);
		assertEquals("Unexpected end", result.getRight(), sdf.format(c.getTime()));
	}
	
	@Test
	public void parameter_null_map_null__value_is_calulated() throws Exception {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2020, Calendar.AUGUST, 15);
		
		Measure measure = new Measure();
		Map<String, Parameter> parameterOverrides = null;

		Pair<String, String> result = new DefaultMeasurementPeriodStrategy()
				.setNow(c.getTime()).getMeasurementPeriod(measure, parameterOverrides);
		
		DateFormat sdf = new SimpleDateFormat(DefaultMeasurementPeriodStrategy.TARGET_DATE_FORMAT);
		assertEquals("Unexpected end", result.getRight(), sdf.format(c.getTime()));
	}
	
	@Test
	public void parameter_not_null_date___value_used() {

		Measure measure = new Measure();
		Map<String, Parameter> parameterOverrides = Collections.singletonMap(DefaultMeasurementPeriodStrategy.DEFAULT_MEASUREMENT_PERIOD_PARAMETER,
				new IntervalParameter(new DateParameter("2020-03-14"), true, new DateParameter("2020-09-14"), true));

		Pair<String, String> result = new DefaultMeasurementPeriodStrategy()
				.getMeasurementPeriod(measure, parameterOverrides);
		assertEquals("Unexpected start", "2020-03-14", result.getLeft() );
		assertEquals("Unexpected end", "2020-09-14", result.getRight() );
	}
	
	@Test
	public void parameter_not_null_datetime_with_ampersand___value_used() {

		Measure measure = new Measure();
		Map<String, Parameter> parameterOverrides = Collections.singletonMap(DefaultMeasurementPeriodStrategy.DEFAULT_MEASUREMENT_PERIOD_PARAMETER,
				new IntervalParameter(new DatetimeParameter("@2020-01-01T00:00:00.0"), true, new DatetimeParameter("@2021-01-01T00:00:00.0"), true));

		Pair<String, String> result = new DefaultMeasurementPeriodStrategy()
				.getMeasurementPeriod(measure, parameterOverrides);
		assertEquals("Unexpected start", "2020-01-01", result.getLeft() );
		assertEquals("Unexpected end", "2021-01-01", result.getRight() );
	}


	private void runGetMeasurementPeriodTest(String parameterName, String start, String end) {
		Measure measure = new Measure();
		Map<String, Parameter> parameterOverrides = Collections.singletonMap(parameterName,
				new IntervalParameter(new DateParameter(start), true, new DateParameter(end), true));

		Pair<String, String> result = new DefaultMeasurementPeriodStrategy()
				.setMeasurementPeriodParameter(parameterName).getMeasurementPeriod(measure, parameterOverrides);
		assertEquals("Unexpected start", result.getLeft(), start);
		assertEquals("Unexpected end", result.getRight(), end);
	}

	private Pair<String, String> runCalculateMeasurementPeriodTest(DefaultMeasurementPeriodStrategy strategy) throws ParseException {
		Pair<String, String> period = strategy.calculateMeasurementPeriod();
		assertStartBeforeEnd(period);
		return period;
	}

	private void assertStartBeforeEnd(Pair<String, String> period) throws ParseException {
		SimpleDateFormat sdf = getDateFormat();
		Date start = sdf.parse(period.getLeft());
		Date end = sdf.parse(period.getRight());

		Calendar cStart = Calendar.getInstance();
		cStart.setTime(start);

		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime(end);

		assertTrue(cStart.before(cEnd));
	}

	private SimpleDateFormat getDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(DefaultMeasurementPeriodStrategy.TARGET_DATE_FORMAT);
		return sdf;
	}
}
