/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.valueset;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CodeSystemLookupTest {
	
	@Test
	public void testCodeSystemLookup() {
		assertEquals("http://terminology.hl7.org/CodeSystem/v3-ActCode", CodeSystemLookup.getUrlFromName("ActCode"));
		assertEquals("http://www.ama-assn.org/go/cpt", CodeSystemLookup.getUrlFromName("CPT"));
		assertEquals("http://terminology.hl7.org/CodeSystem/umls", CodeSystemLookup.getUrlFromName("UMLS"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCodeSystem() {
		CodeSystemLookup.getUrlFromName("FakeCodeSystem");
	}
}
