/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.version;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

public class SemanticVersionTest {

	@Test
	public void testNullArgumentToCreate_returnsEmptyOptional() {
		Optional<SemanticVersion> semanticVersionOptional = SemanticVersion.create(null);
		assertFalse(semanticVersionOptional.isPresent());
	}

	@Test
	public void semanticVersionDoesNotMatchPattern_returnsEmptyOptional() {
		Optional<SemanticVersion> semanticVersionOptional = SemanticVersion.create("1.2.d");
		assertFalse(semanticVersionOptional.isPresent());
	}

	@Test
	public void semanticVersionMatchesPattern_returnsOptionalValue() {
		Optional<SemanticVersion> semanticVersionOptional = SemanticVersion.create("1.2.3");
		assertTrue(semanticVersionOptional.isPresent());
		assertEquals(new SemanticVersion(1, 2, 3), semanticVersionOptional.get());
	}

	@Test
	public void testMeasureversionCompareTo() {
		SemanticVersion version1_9_9 = SemanticVersion.create("1.9.9").get();
		SemanticVersion version2_0_0 = SemanticVersion.create("2.0.0").get();
		SemanticVersion version3_0_9 = SemanticVersion.create("3.0.9").get();
		SemanticVersion version3_1_0 = SemanticVersion.create("3.1.0").get();
		SemanticVersion version3_1_1 = SemanticVersion.create("3.1.1").get();

		// Major version decides
		assertTrue(version1_9_9.compareTo(version2_0_0) < 0);
		assertTrue(version2_0_0.compareTo(version1_9_9) > 0);

		// Minor version decides
		assertTrue(version3_0_9.compareTo(version3_1_0) < 0);
		assertTrue(version3_1_0.compareTo(version3_0_9) > 0);

		// Patch decides
		assertTrue(version3_1_0.compareTo(version3_1_1) < 0);
		assertTrue(version3_1_1.compareTo(version3_1_0) > 0);

		// Compare to self
		assertEquals(0, version1_9_9.compareTo(new SemanticVersion(1, 9, 9)));
		assertEquals(0, version3_1_0.compareTo(new SemanticVersion(3, 1, 0)));

	}

	@Test
	public void testToString() {
		SemanticVersion semanticVersion = SemanticVersion.create("1.2.3").orElse(null);

		assertNotNull(semanticVersion);
		assertEquals("1.2.3", semanticVersion.toString());
	}
}