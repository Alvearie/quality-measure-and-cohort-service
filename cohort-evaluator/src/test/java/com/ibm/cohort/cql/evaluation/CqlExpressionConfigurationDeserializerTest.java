/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CqlExpressionConfigurationDeserializerTest {

	@Test
	public void testDeserializerStringOnly() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		CqlExpressionConfiguration actual = mapper.readValue("\"aName\"", CqlExpressionConfiguration.class);
		
		CqlExpressionConfiguration expected = new CqlExpressionConfiguration();
		expected.setName("aName");
		assertEquals(expected, actual);
	}

	@Test
	public void testDeserializerConfiguredName() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		CqlExpressionConfiguration actual = mapper.readValue("{\"name\":\"aName\"}", CqlExpressionConfiguration.class);

		CqlExpressionConfiguration expected = new CqlExpressionConfiguration();
		expected.setName("aName");
		assertEquals(expected, actual);
	}

	@Test
	public void testDeserializerConfiguredNameAndOutputColumn() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		CqlExpressionConfiguration actual = mapper.readValue("{\"name\":\"aName\", \"outputColumn\":\"colA\"}", CqlExpressionConfiguration.class);

		CqlExpressionConfiguration expected = new CqlExpressionConfiguration();
		expected.setName("aName");
		expected.setoutputColumn("colA");
		assertEquals(expected, actual);
	}

	@Test
	public void testDeserializerConfiguredObjectNoName() {
		ObjectMapper mapper = new ObjectMapper();
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.readValue("{\"outputColumn\":\"colA\"}", CqlExpressionConfiguration.class));
		assertTrue(ex.getMessage().contains("must specify 'name' field"));
	}

	@Test
	public void testDeserializerConfiguredNameNonString() {
		ObjectMapper mapper = new ObjectMapper();
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.readValue("{\"name\":1}", CqlExpressionConfiguration.class));
		assertTrue(ex.getMessage().contains("Error parsing 'name'"));
	}

	@Test
	public void testDeserializerConfiguredOutputColumnNonString() {
		ObjectMapper mapper = new ObjectMapper();
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.readValue("{\"name\":\"aName\",\"outputColumn\":1}", CqlExpressionConfiguration.class));
		assertTrue(ex.getMessage().contains("Error parsing 'outputColumn'"));
	}

	@Test
	public void testDeserializerIncorrectType() {
		ObjectMapper mapper = new ObjectMapper();
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.readValue("2", CqlExpressionConfiguration.class));
		assertTrue(ex.getMessage().contains("Expected string or object"));
	}

	@Test
	public void testDeserializerUnrecognizedField() {
		ObjectMapper mapper = new ObjectMapper();
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.readValue("{\"name\":\"aName\",\"badField\":\"val\"}", CqlExpressionConfiguration.class));
		assertTrue(ex.getMessage().contains("Unrecognized field"));
	}
}