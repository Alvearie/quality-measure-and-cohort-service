package com.ibm.cohort.cli.input;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MeasureIdWithParametersTest {
	
	@Test
	public void testValidWithoutParameters_executionSucceeds() throws Exception {
		MeasureIdWithParameters measureIdWithParameters = createMeasureIdWithParameters("{\"measureId\":\"1234\"}");
		measureIdWithParameters.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalid_missingMeasureId_throwsException() throws Exception {
		MeasureIdWithParameters measureIdWithParameters = createMeasureIdWithParameters("{}");
		measureIdWithParameters.validate();
	}
	
	@Test
	public void testEmptyParameters_executionSucceeds() throws Exception {
		MeasureIdWithParameters measureIdWithParameters = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":[]}");
		measureIdWithParameters.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSingleParameter_throwsException() throws Exception {
		MeasureIdWithParameters measureIdWithParameters = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":[{\"name\":\"p1\"}]}");
		measureIdWithParameters.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidAndInvalidParameters_throwsException() throws Exception {
		MeasureIdWithParameters measureIdWithParameters = createMeasureIdWithParameters("{\"measureId\":\"1234\",\"parameters\":[{\"name\":\"p1\",\"type\":\"integer\",\"value\":\"1\"},{\"name\":\"p2\"}]}");
		measureIdWithParameters.validate();
	}

	private MeasureIdWithParameters createMeasureIdWithParameters(String inputString) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(inputString, MeasureIdWithParameters.class);
	}
}