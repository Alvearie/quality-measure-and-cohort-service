package com.ibm.cohort.engine.helpers;


import static org.junit.Assert.assertTrue;

import java.time.ZoneOffset;

import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.DateTime;

public class MeasurementPeriodHelperTest {

	@Test
	public void testPeriodStartYearOnly__returnsStartOfYear() {
		String input = "2014";

		DateTime expected = new DateTime("2014-01-01T00:00:00.0", ZoneOffset.UTC);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodStart(input)));
	}

	@Test
	public void testPeriodStartYearAndMonth__returnsStartOfMonth() {
		String input = "2014-07";

		DateTime expected = new DateTime("2014-07-01T00:00:00.0", ZoneOffset.UTC);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodStart(input)));
	}

	@Test
	public void testPeriodStartYearMonthDay__returnsStartOfDay() {
		String input = "2014-07-20";

		DateTime expected = new DateTime("2014-07-20T00:00:00.0", ZoneOffset.UTC);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodStart(input)));
	}

	@Test
	public void testPeriodStartAdditionalPrecision__returnsStartOfDay() {
		String input = "2014-07-20T12:45:45.9";

		DateTime expected = new DateTime("2014-07-20T00:00:00.0", ZoneOffset.UTC);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodStart(input)));
	}

	@Test
	public void testPeriodStartWithTimezoneOffset__returnsStartOfYear() {
		String inputDate = "2014";
		ZoneOffset inputZoneOffset = ZoneOffset.ofHours(4);


		DateTime expected = new DateTime("2014-01-01T00:00:00.0", inputZoneOffset);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodStart(inputDate, inputZoneOffset)));
	}

	@Test
	public void testPeriodEndYearOnly__returnsEndOfYear() {
		String input = "2014";

		DateTime expected = new DateTime("2014-12-31T23:59:59.999", ZoneOffset.UTC);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodEnd(input)));
	}

	@Test
	public void testPeriodEndYearAndMonth__returnsEndOfMonth() {
		String input = "2014-07";

		DateTime expected = new DateTime("2014-07-31T23:59:59.999", ZoneOffset.UTC);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodEnd(input)));
	}

	@Test
	public void testPeriodEndYearMonthDay__returnsEndOfDay() {
		String input = "2014-07-20";

		DateTime expected = new DateTime("2014-07-20T23:59:59.999", ZoneOffset.UTC);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodEnd(input)));
	}

	@Test
	public void testPeriodEndAdditionalPrecision__returnsEndOfDay() {
		String input = "2014-07-20T14:45:45.9";

		DateTime expected = new DateTime("2014-07-20T23:59:59.999", ZoneOffset.UTC);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodEnd(input)));
	}

	@Test
	public void testPeriodEndWithTimezoneOffset__returnsEndOfYear() {
		String inputDate = "2014";
		ZoneOffset inputZoneOffset = ZoneOffset.ofHours(4);

		DateTime expected = new DateTime("2014-12-31T23:59:59.999", inputZoneOffset);
		assertTrue(expected.equal(MeasurementPeriodHelper.getPeriodEnd(inputDate, inputZoneOffset)));
	}
}