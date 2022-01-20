/*
 * (C) Copyright IBM Corp. 2022, 2022
 *  
 * SPDX-License-Identifier: Apache-2.0
 *  
 */

package com.ibm.cohort.cli;


import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FhirResourceToolingCLIDriverTest {
	@Test
	public void testRunnabblePrograms() {
		FhirResourceToolingCLIDriver driver = new FhirResourceToolingCLIDriver();
		assertTrue(driver.getRunnableProgram(FhirResourceToolingCLIDriver.MEASURE_IMPORTER) instanceof MeasureImporterRunner);
		assertTrue(driver.getRunnableProgram(FhirResourceToolingCLIDriver.VALUE_SET_IMPORTER) instanceof ValueSetImporterRunner);
	}

	@Test
	public void testUnsupportedCommand() {
		FhirResourceToolingCLIDriver driver = new FhirResourceToolingCLIDriver();
		assertThrows(UnsupportedOperationException.class, () -> driver.getRunnableProgram("badcommand"));
	}
}