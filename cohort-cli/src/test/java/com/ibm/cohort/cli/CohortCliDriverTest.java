package com.ibm.cohort.cli;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CohortCliDriverTest {
	@Test
	public void testRunnabblePrograms() {
		CohortCliDriver driver = new CohortCliDriver();
		assertTrue(driver.getRunnableProgram(CohortCliDriver.COHORT_CLI) instanceof CohortCLIRunner);
		assertTrue(driver.getRunnableProgram(CohortCliDriver.MEASURE_CLI) instanceof MeasureCLIRunner);
		assertTrue(driver.getRunnableProgram(CohortCliDriver.TRANSLATION_CLI) instanceof TranslationCLIRunner);
	}

	@Test
	public void testUnsupportedCommand() {
		CohortCliDriver driver = new CohortCliDriver();
		assertThrows(UnsupportedOperationException.class, () -> driver.getRunnableProgram("badcommand"));
	}
}