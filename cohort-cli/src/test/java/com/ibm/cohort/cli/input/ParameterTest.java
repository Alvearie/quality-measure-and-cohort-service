/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParameterTest {
	@Test
	public void testValidNonIntervalParameter_validationSucceedsWithoutException() throws Exception {
		Parameter parameter = createParameter("{\"name\":\"p1\",\"type\":\"integer\",\"valueset\":\"1\"}");
		parameter.validate();
	}

	@Test
	public void testValidIntervalParameter_validationSucceedsWithoutException() throws Exception {
		Parameter parameter = createParameter("{\"name\":\"p1\",\"type\":\"interval\",\"start\":\"1\",\"end\":\"2\",\"subtype\":\"integer\"}");
		parameter.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidParameter_missingName_throwsException() throws Exception {
		Parameter parameter = createParameter("{\"type\":\"integer\",\"valueset\":\"1\"}");
		parameter.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidParameter_missingType_throwsException() throws Exception {
		Parameter parameter = createParameter("{\"name\":\"p1\",\"valueset\":\"1\"}");
		parameter.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidParameter_missingValue_throwsException() throws Exception {
		Parameter parameter = createParameter("{\"name\":\"p1\",\"type\":\"integer\"}");
		parameter.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidIntervalParameter_missingSubtype_throwsException() throws Exception {
		Parameter parameter = createParameter("{\"name\":\"p1\",\"type\":\"interval\",\"start\":\"1\",\"end\":\"2\"}");
		parameter.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidIntervalParameter_missingStart_throwsException() throws Exception {
		Parameter parameter = createParameter("{\"name\":\"p1\",\"type\":\"interval\",\"end\":\"2\",\"subtype\":\"integer\"}");
		parameter.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidIntervalParameter_missingEnd_throwsException() throws Exception {
		Parameter parameter = createParameter("{\"name\":\"p1\",\"type\":\"interval\",\"start\":\"1\",\"subtype\":\"integer\"}");
		parameter.validate();
	}
	
	private Parameter createParameter(String parameterString) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(parameterString, Parameter.class);
	}
}