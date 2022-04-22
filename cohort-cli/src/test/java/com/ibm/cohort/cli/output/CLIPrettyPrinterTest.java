/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;

public class CLIPrettyPrinterTest {
	private final CLIPrettyPrinter prettyPrinter = new CLIPrettyPrinter();
	
	@Test
	public void testSingleResult() {
		Map<String, Object> result = new HashMap<>();
		result.put("define1", 1);

		String actual = prettyPrinter.prettyPrintResult(new CqlEvaluationResult(result));
		assertEquals("Expression: \"define1\", Result: 1\n", actual);
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
	public void testBasicList() {
		List<Integer> integers = Arrays.asList(1, 2, 3, 4);

		assertEquals("[1, 2, 3, 4]", prettyPrinter.prettyPrintValue(integers));
	}

	@Test
	public void testResourceList() {
		Patient patient1 = new Patient();
		patient1.setId("Patient/id1");

		Patient patient2 = new Patient();
		patient2.setId("Patient/id2");

		assertEquals("[Patient/id1, Patient/id2]", prettyPrinter.prettyPrintValue(Arrays.asList(patient1, patient2)));
	}
}