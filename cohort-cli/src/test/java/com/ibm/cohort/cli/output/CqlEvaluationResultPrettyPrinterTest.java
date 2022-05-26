/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Tuple;

import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;

public class CqlEvaluationResultPrettyPrinterTest {
	private final CqlEvaluationResultPrettyPrinter prettyPrinter = mock(CqlEvaluationResultPrettyPrinter.class);
	
	@Test
	public void testSingleResult() {
		Map<String, Object> result = new HashMap<>();
		result.put("define1", 1);

		String actual = prettyPrinter.prettyPrintResult(new CqlEvaluationResult(result));
		assertEquals("Expression: \"define1\", Result: 1\n", actual);
	}

	@Test
	public void testSingleResultString() {
		Map<String, Object> result = new HashMap<>();
		result.put("define1", "1");

		String actual = prettyPrinter.prettyPrintResult(new CqlEvaluationResult(result));
		assertEquals("Expression: \"define1\", Result: \"1\"\n", actual);
	}

	@Test
	public void testMultipleResults() {
		Map<String, Object> result = new HashMap<>();
		result.put("define1", 1);
		result.put("define2", 2);

		String actual = prettyPrinter.prettyPrintResult(new CqlEvaluationResult(result));
		assertTrue(actual.contains("Expression: \"define1\", Result: 1"));
		assertTrue(actual.contains("Expression: \"define2\", Result: 2"));
	}

	@Test
	public void testResource() {
		String expected = "Patient/12345";

		Patient patient = new Patient();
		patient.setId(expected);

		assertEquals(expected, prettyPrinter.prettyPrintValue(patient));
	}

	@Test
	public void testResourceNull() {
		assertEquals("null", prettyPrinter.prettyPrintValue(null));
	}
	
	@Test
	public void testBasicType() {
		String expected = "123";

		assertEquals(expected, prettyPrinter.prettyPrintValue(123));
	}
	
	@Test
	public void testTuple() {
		Patient patient = new Patient();
		patient.setId("Patient/id1");
		
		Tuple tuple = new Tuple();
		
		LinkedHashMap<String, Object> elements = new LinkedHashMap<>();
		elements.put("definition", "stuff");
		elements.put("value", patient);
		tuple.setElements(elements);

		assertEquals("Tuple { \"definition\": \"stuff\",  \"value\": Patient/id1 }", prettyPrinter.prettyPrintValue(tuple));
	}
}