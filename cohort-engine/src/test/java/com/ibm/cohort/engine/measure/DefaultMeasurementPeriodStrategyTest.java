/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class DefaultMeasurementPeriodStrategyTest {

	@Test
	public void default_values__start_before_end() throws Exception {
		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy();
		runTest(strategy);
	}

	@Test
	public void positive_amount__start_before_end() throws Exception {
		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy(Calendar.MONTH, 1);
		runTest(strategy);
	}
	
	@Test
	public void negative_amount__start_before_end() throws Exception {
		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy(Calendar.MONTH, -1);
		runTest(strategy);
	}
	
	@Test
	public void fixed_now_negative_amount__is_correct_end() throws Exception {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2020, Calendar.OCTOBER, 20);
		
		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy(Calendar.MONTH, -1);
		strategy.setNow(c.getTime());
		
		Pair<String,String> period = runTest(strategy);
		
		SimpleDateFormat sdf = getDateFormat();
		String expected = sdf.format( c.getTime() );
		assertEquals( expected, period.getRight() );
	}
	
	@Test
	public void fixed_now_positive_amount__is_correct_start() throws Exception {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2020, Calendar.OCTOBER, 20);
		
		DefaultMeasurementPeriodStrategy strategy = new DefaultMeasurementPeriodStrategy(Calendar.MONTH, 1);
		strategy.setNow(c.getTime());
		
		Pair<String,String> period = runTest(strategy);
		
		SimpleDateFormat sdf = getDateFormat();
		String expected = sdf.format( c.getTime() );
		assertEquals( expected, period.getLeft() );
	}
	
	private Pair<String,String> runTest(DefaultMeasurementPeriodStrategy strategy) throws ParseException {
		Pair<String, String> period = strategy.getMeasurementPeriod();
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
		
		assertTrue( cStart.before(cEnd) );
	}

	private SimpleDateFormat getDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(DefaultMeasurementPeriodStrategy.TARGET_DATE_FORMAT);
		return sdf;
	}
}
