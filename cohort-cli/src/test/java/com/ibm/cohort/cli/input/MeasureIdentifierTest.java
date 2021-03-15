/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MeasureIdentifierTest {
	@Test
	public void testIdentifierWithValue_executionSucceeds() throws Exception {
		MeasureIdentifier measureIdentifier = createMeasureIdentifier("{\"value\":\"12345\"}");
		measureIdentifier.validate();
	}

	@Test
	public void testIdentifierWithValueAndSystem_executionSucceeds() throws Exception {
		MeasureIdentifier measureIdentifier = createMeasureIdentifier("{\"value\":\"12345\",\"system\":\"system1\"}");
		measureIdentifier.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIdentifierMissingValue_throwsException() throws Exception {
		MeasureIdentifier measureIdentifier = createMeasureIdentifier("{\"system\":\"system1\"}");
		measureIdentifier.validate();
	}

	private MeasureIdentifier createMeasureIdentifier(String inputString) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(inputString, MeasureIdentifier.class);
	}
}