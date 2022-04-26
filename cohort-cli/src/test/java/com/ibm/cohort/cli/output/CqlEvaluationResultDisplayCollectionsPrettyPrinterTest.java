/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli.output;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

public class CqlEvaluationResultDisplayCollectionsPrettyPrinterTest {
	private final CqlEvaluationResultDisplayCollectionsPrettyPrinter prettyPrinter = new CqlEvaluationResultDisplayCollectionsPrettyPrinter();

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

	@Test
	public void testResourceEmptyList() {
		assertEquals("[]", prettyPrinter.prettyPrintValue(new ArrayList()));
	}
}