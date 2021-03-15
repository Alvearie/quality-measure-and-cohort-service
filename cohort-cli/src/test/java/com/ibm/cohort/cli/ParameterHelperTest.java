/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.ibm.cohort.engine.parameter.BooleanParameter;
import com.ibm.cohort.engine.parameter.CodeParameter;
import com.ibm.cohort.engine.parameter.DatetimeParameter;
import com.ibm.cohort.engine.parameter.DecimalParameter;
import com.ibm.cohort.engine.parameter.IntegerParameter;
import com.ibm.cohort.engine.parameter.IntervalParameter;
import com.ibm.cohort.engine.parameter.Parameter;
import com.ibm.cohort.engine.parameter.QuantityParameter;
import com.ibm.cohort.engine.parameter.StringParameter;
import com.ibm.cohort.engine.parameter.TimeParameter;

public class ParameterHelperTest {
	@Test
	public void testResolveIntegerParameter() {
		Map<String, Parameter> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:integer:40"));
		assertEquals(1, params.size());
		IntegerParameter p = (IntegerParameter) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 40, p.getValue());
	}

	@Test
	public void testResolveDecimalParameter() {
		Map<String, Parameter> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:decimal:40.0"));
		assertEquals(1, params.size());
		DecimalParameter p = (DecimalParameter) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "40.0", p.getValue());
	}

	@Test
	public void testResolveBooleanParameter() {
		Map<String, Parameter> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:boolean:true"));
		assertEquals(1, params.size());
		BooleanParameter p = (BooleanParameter) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", true, p.getValue());
	}

	@Test
	public void testResolveStringParameter() {
		Map<String, Parameter> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:string:I have the:delimiter"));
		assertEquals(1, params.size());
		StringParameter p = (StringParameter) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "I have the:delimiter", p.getValue());
	}

	@Test
	public void testResolveDateTimeParameter() {
		Map<String, Parameter> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:datetime:@2020-09-27T12:13:14"));
		assertEquals(1, params.size());
		DatetimeParameter p = (DatetimeParameter) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "@2020-09-27T12:13:14", p.getValue());
	}

	@Test
	public void testResolveTimeParameter() {
		Map<String, Parameter> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:time:T12:13:14"));
		assertEquals(1, params.size());
		TimeParameter p = (TimeParameter) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "T12:13:14", p.getValue());

	}

	@Test
	public void testResolveQuantityParameter() {
		Map<String, Parameter> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:quantity:100:mg/mL"));
		assertEquals(1, params.size());
		QuantityParameter p = (QuantityParameter) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "100", p.getAmount().toString());
		assertEquals("Unexpected value", "mg/mL", p.getUnit().toString());
	}

	@Test
	public void testResolveCodeParameter() {
		Map<String, Parameter> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:code:1.2.3:SNOMEDCT:Hernia"));
		assertEquals(1, params.size());
		CodeParameter p = (CodeParameter) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "1.2.3", p.getValue());
		assertEquals("Unexpected value", "SNOMEDCT", p.getSystem());
		assertEquals("Unexpected value", "Hernia", p.getDisplay());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testResolveConceptParameter() {
		ParameterHelper.parseParameterArguments(Arrays.asList("test:concept:not right now"));
	}

	@Test
	public void testResolveIntervalIntegerParameter() {
		Map<String, Parameter> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:interval:integer,10,20"));
		assertEquals(1, params.size());
		IntervalParameter p = (IntervalParameter) params.get("test");
		assertEquals(10, ((IntegerParameter)p.getStart()).getValue());
		assertEquals(20, ((IntegerParameter)p.getEnd()).getValue());
	}

	@Test
	public void testResolveIntervalDecimalParameter() {
		Map<String, Parameter> params = ParameterHelper.parseParameterArguments(Arrays.asList("test:interval:decimal,10,20"));
		assertEquals(1, params.size());
		IntervalParameter p = (IntervalParameter) params.get("test");
		assertEquals("10", ((DecimalParameter) p.getStart()).getValue());
		assertEquals("20", ((DecimalParameter) p.getEnd()).getValue());
	}

	@Test
	public void testResolveIntervalQuantityParameter() {
		Map<String, Parameter> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:interval:quantity,10:mg/mL,20:mg/mL"));
		assertEquals(1, params.size());
		IntervalParameter p = (IntervalParameter) params.get("test");
		assertEquals("10", ((QuantityParameter) p.getStart()).getAmount());
		assertEquals("mg/mL", ((QuantityParameter) p.getStart()).getUnit());
		assertEquals("20", ((QuantityParameter) p.getEnd()).getAmount());
		assertEquals("mg/mL", ((QuantityParameter) p.getEnd()).getUnit());
	}

	@Test
	public void testResolveIntervalDatetimeParameter() {
		Map<String, Parameter> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:interval:datetime,@2020-01-02T12:13:14,@2021-02-03T22:33:44"));
		assertEquals(1, params.size());
		IntervalParameter p = (IntervalParameter) params.get("test");
		DatetimeParameter start = (DatetimeParameter) p.getStart();
		assertEquals("@2020-01-02T12:13:14", start.getValue());

		DatetimeParameter end = (DatetimeParameter) p.getEnd();
		assertEquals("@2021-02-03T22:33:44", end.getValue());
	}

	@Test
	public void testResolveIntervalTimeParameter() {
		Map<String, Parameter> params = ParameterHelper
				.parseParameterArguments(Arrays.asList("test:interval:time,T12:13:14,T22:33:44"));
		assertEquals(1, params.size());
		IntervalParameter p = (IntervalParameter) params.get("test");
		TimeParameter start = (TimeParameter) p.getStart();
		assertEquals("T12:13:14", start.getValue());

		TimeParameter end = (TimeParameter) p.getEnd();
		assertEquals("T22:33:44", end.getValue());

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
