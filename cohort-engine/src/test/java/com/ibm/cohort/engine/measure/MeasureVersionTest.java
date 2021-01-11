package com.ibm.cohort.engine.measure;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

public class MeasureVersionTest {

	@Test
	public void measureVersionDoesNotMatchPattern_returnsEmptyOptional() {
		Optional<MeasureVersion> measureVersionOptional = MeasureVersion.create("1.2.d");
		assertFalse(measureVersionOptional.isPresent());
	}

	@Test
	public void measureVersionMatchesPattern_returnsOptionalValue() {
		Optional<MeasureVersion> measureVersionOptional = MeasureVersion.create("1.2.3");
		assertTrue(measureVersionOptional.isPresent());
		assertEquals(new MeasureVersion(1, 2, 3), measureVersionOptional.get());
	}

	@Test
	public void testMeasureversionCompareTo() {
		MeasureVersion version1_9_9 = MeasureVersion.create("1.9.9").get();
		MeasureVersion version2_0_0 = MeasureVersion.create("2.0.0").get();
		MeasureVersion version3_0_9 = MeasureVersion.create("3.0.9").get();
		MeasureVersion version3_1_0 = MeasureVersion.create("3.1.0").get();
		MeasureVersion version3_1_1 = MeasureVersion.create("3.1.1").get();

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
		assertEquals(0, version1_9_9.compareTo(version1_9_9));
		assertEquals(0, version3_1_0.compareTo(version3_1_0));

	}
}