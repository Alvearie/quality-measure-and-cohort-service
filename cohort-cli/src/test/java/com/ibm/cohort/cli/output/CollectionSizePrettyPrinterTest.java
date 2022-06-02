/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli.output;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Tuple;

public class CollectionSizePrettyPrinterTest {
	private final CollectionSizePrettyPrinter prettyPrinter = new CollectionSizePrettyPrinter();
	
	@Test
	public void testBasicList() {
		List<Integer> integers = Arrays.asList(1, 2, 3, 4);

		assertEquals("Collection: 4", prettyPrinter.prettyPrintValue(integers));
	}

	@Test
	public void testResourceList() {
		Patient patient1 = new Patient();
		patient1.setId("Patient/id1");

		Patient patient2 = new Patient();
		patient2.setId("Patient/id2");

		assertEquals("Collection: 2", prettyPrinter.prettyPrintValue(Arrays.asList(patient1, patient2)));
	}

	@Test
	public void testResourceEmptyList() {
		assertEquals("Collection: 0", prettyPrinter.prettyPrintValue(Collections.emptyList()));
	}

	@Test
	public void testTupleWithList() {
		Patient patient1 = new Patient();
		patient1.setId("Patient/id1");

		Patient patient2 = new Patient();
		patient2.setId("Patient/id2");

		Tuple tuple = new Tuple();

		LinkedHashMap<String, Object> elements = new LinkedHashMap<>();
		elements.put("definition", "stuff");
		elements.put("value", Arrays.asList(patient1, patient2));
		tuple.setElements(elements);

		assertEquals("Tuple { \"definition\": \"stuff\",  \"value\": Collection: 2 }", prettyPrinter.prettyPrintValue(tuple));
	}
}