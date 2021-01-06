/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MeasureConfigurationTest {
	
	@Test
	public void testValidWithoutParameters_executionSucceeds() throws Exception {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\"}");
		measureConfiguration.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalid_missingMeasureId_throwsException() throws Exception {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{}");
		measureConfiguration.validate();
	}
	
	@Test
	public void testEmptyParameters_executionSucceeds() throws Exception {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":[]}");
		measureConfiguration.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSingleParameter_throwsException() throws Exception {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":[{\"name\":\"p1\"}]}");
		measureConfiguration.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidAndInvalidParameters_throwsException() throws Exception {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":[{\"name\":\"p1\",\"type\":\"integer\",\"value\":\"1\"},{\"name\":\"p2\"}]}");
		measureConfiguration.validate();
	}

	private MeasureConfiguration createMeasureIdWithParameters(String inputString) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(inputString, MeasureConfiguration.class);
	}
}