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
	public void testInvalid_missingMeasureIdOrIdentifier_throwsException() throws Exception {
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
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":[{\"name\":\"p1\",\"type\":\"integer\",\"valueset\":\"1\"},{\"name\":\"p2\"}]}");
		measureConfiguration.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIdAndIdentifier_throwsException() throws Exception {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"identifier\":{\"valueset\":\"identifier1\"}}");
		measureConfiguration.validate();
	}

	@Test
	public void testWithIdentifierValueOnly_executionSucceeds() throws Exception {
		MeasureConfiguration measureConfiguration = createMeasureIdWithParameters("{\"identifier\":{\"valueset\":\"identifier1\"}}");
		measureConfiguration.validate();
	}

	private MeasureConfiguration createMeasureIdWithParameters(String inputString) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(inputString, MeasureConfiguration.class);
	}
}