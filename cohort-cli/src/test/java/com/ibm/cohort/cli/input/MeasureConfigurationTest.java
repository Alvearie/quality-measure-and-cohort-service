/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class MeasureConfigurationTest {
	
	private Object[] successfulParameters() {
		return new Object[] {
			new Object[] {"{\"measureId\":\"1234\"}"},
			new Object[] {"{\"measureId\":\"1234\",\"parameters\":{}}"},
			new Object[] {"{\"identifier\":{\"value\":\"identifier1\"}}"}
		};
	}
	
	@Test(expected = Test.None.class /* no exception expected */)
	@Parameters(method = "successfulParameters")
	public void testSuccessfulParameters(String parameters) throws JsonProcessingException {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters(parameters);
		measureConfiguration.validate();
	}
	
	
	
//	@Test
//	public void testValidWithoutParameters_executionSucceeds() throws Exception {
//		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\"}");
//		measureConfiguration.validate();
//	}
//
//	
	@Test
	public void testEmptyParameters_executionSucceeds() throws Exception {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":{}}");
		measureConfiguration.validate();
	}

//
//	@Test
//	public void testWithIdentifierValueOnly_executionSucceeds() throws Exception {
//		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"identifier\":{\"value\":\"identifier1\"}}");
//		measureConfiguration.validate();
//	}
	
//
//	@Test(expected = InvalidTypeIdException.class)
//	public void testInvalidSingleParameter_throwsException() throws Exception {
//		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":{\"invalid\":{\"type\": \"unknown\", \"name\":\"p1\"}}}");
//		measureConfiguration.validate();
//	}
//
//	@Test(expected = InvalidTypeIdException.class)
//	public void testValidAndInvalidParameters_throwsException() throws Exception {
//
//		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":{\"p1\": { \"type\":\"integer\",\"value\":1},\"p2\":{\"type\": \"unknown\"}}}");
//		measureConfiguration.validate();
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void testInvalid_missingMeasureIdOrIdentifier_throwsException() throws Exception {
//		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{}");
//		measureConfiguration.validate();
//	}
//	@Test(expected = IllegalArgumentException.class)
//	public void testIdAndIdentifier_throwsException() throws Exception {
//		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"identifier\":{\"value\":\"identifier1\"}}");
//		measureConfiguration.validate();
//	}

	private MeasureConfiguration createMeasureIdWithParameters(String inputString) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(inputString, MeasureConfiguration.class);
	}
}