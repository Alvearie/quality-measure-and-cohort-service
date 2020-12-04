/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Time;

public class ParameterHelperTest {
	@Test
	public void testResolveIntegerParameter() {
		Map<String, Object> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:integer:40"));
		assertEquals(1, params.size());
		Integer p = (Integer) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 40, p.intValue());
	}

	@Test
	public void testResolveDecimalParameter() {
		Map<String, Object> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:decimal:40.0"));
		assertEquals(1, params.size());
		BigDecimal p = (BigDecimal) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 40, p.intValue());
	}

	@Test
	public void testResolveBooleanParameter() {
		Map<String, Object> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:boolean:true"));
		assertEquals(1, params.size());
		Boolean p = (Boolean) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", true, p.booleanValue());
	}

	@Test
	public void testResolveStringParameter() {
		Map<String, Object> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:string:I have the:delimiter"));
		assertEquals(1, params.size());
		String p = (String) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "I have the:delimiter", p);
	}

	@Test
	public void testResolveDateTimeParameter() {
		Map<String, Object> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:datetime:@2020-09-27T12:13:14"));
		assertEquals(1, params.size());
		DateTime p = (DateTime) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 2020, p.getDateTime().getYear());
		assertEquals("Unexpected value", 9, p.getDateTime().getMonthValue());
		assertEquals("Unexpected value", 27, p.getDateTime().getDayOfMonth());
		assertEquals("Unexpected value", 12, p.getDateTime().getHour());
		assertEquals("Unexpected value", 13, p.getDateTime().getMinute());
		assertEquals("Unexpected value", 14, p.getDateTime().getSecond());
	}

	@Test
	public void testResolveTimeParameter() {
		Map<String, Object> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:time:T12:13:14"));
		assertEquals(1, params.size());
		Time p = (Time) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 12, p.getTime().getHour());
		assertEquals("Unexpected value", 13, p.getTime().getMinute());
		assertEquals("Unexpected value", 14, p.getTime().getSecond());
	}

	@Test
	public void testResolveQuantityParameter() {
		Map<String, Object> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:quantity:100:mg/mL"));
		assertEquals(1, params.size());
		Quantity p = (Quantity) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "100", p.getValue().toString());
		assertEquals("Unexpected value", "mg/mL", p.getUnit().toString());
	}

	@Test
	public void testResolveCodeParameter() {
		Map<String, Object> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:code:1.2.3:SNOMEDCT:Hernia"));
		assertEquals(1, params.size());
		Code p = (Code) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "1.2.3", p.getCode());
		assertEquals("Unexpected value", "SNOMEDCT", p.getSystem());
		assertEquals("Unexpected value", "Hernia", p.getDisplay());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testResolveConceptParameter() {
		ParameterHelper.parseParameterArguments(Arrays.asList("test:concept:not right now"));
	}

	@Test
	public void testResolveIntervalIntegerParameter() {
		Map<String, Object> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:interval:integer,10,20"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		assertEquals(10, p.getStart());
		assertEquals(20, p.getEnd());
	}

	@Test
	public void testResolveIntervalDecimalParameter() {
		Map<String, Object> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:interval:decimal,10,20"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		assertEquals(10, ((BigDecimal) p.getStart()).intValue());
		assertEquals(20, ((BigDecimal) p.getEnd()).intValue());
	}

	@Test
	public void testResolveIntervalQuantityParameter() {
		Map<String, Object> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:interval:quantity,10:mg/mL,20:mg/mL"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		assertEquals(10, ((Quantity) p.getStart()).getValue().intValue());
		assertEquals("mg/mL", ((Quantity) p.getStart()).getUnit());
		assertEquals(20, ((Quantity) p.getEnd()).getValue().intValue());
		assertEquals("mg/mL", ((Quantity) p.getEnd()).getUnit());
	}

	@Test
	public void testResolveIntervalDatetimeParameter() {
		Map<String, Object> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:interval:datetime,@2020-01-02T12:13:14,@2021-02-03T22:33:44"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		DateTime start = (DateTime) p.getStart();
		assertEquals(2020, start.getDateTime().getYear());
		assertEquals(1, start.getDateTime().getMonthValue());
		assertEquals(2, start.getDateTime().getDayOfMonth());
		assertEquals(12, start.getDateTime().getHour());
		assertEquals(13, start.getDateTime().getMinute());
		assertEquals(14, start.getDateTime().getSecond());

		DateTime end = (DateTime) p.getEnd();
		assertEquals(2021, end.getDateTime().getYear());
		assertEquals(2, end.getDateTime().getMonthValue());
		assertEquals(3, end.getDateTime().getDayOfMonth());
		assertEquals(22, end.getDateTime().getHour());
		assertEquals(33, end.getDateTime().getMinute());
		assertEquals(44, end.getDateTime().getSecond());
	}

	@Test
	public void testResolveIntervalTimeParameter() {
		Map<String, Object> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:interval:time,T12:13:14,T22:33:44"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		Time start = (Time) p.getStart();
		assertEquals(12, start.getTime().getHour());
		assertEquals(13, start.getTime().getMinute());
		assertEquals(14, start.getTime().getSecond());

		Time end = (Time) p.getEnd();
		assertEquals(22, end.getTime().getHour());
		assertEquals(33, end.getTime().getMinute());
		assertEquals(44, end.getTime().getSecond());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolveIntervalUnsupportedSubtypeParameter() {
		ParameterHelper.parseParameterArguments(Arrays.asList("test:interval:unsupported,a,b"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolveUnsupportedTypeParameter() {
		ParameterHelper.parseParameterArguments(Arrays.asList("test:unsupported:a,b"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolveUnsupportedFormatParameter() {
		ParameterHelper.parseParameterArguments(Arrays.asList("gibberish"));
	}
}
