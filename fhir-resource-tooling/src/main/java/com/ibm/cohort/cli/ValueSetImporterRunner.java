/*
 * (C) Copyright IBM Corp. 2022, 2022
 *  
 * SPDX-License-Identifier: Apache-2.0
 *  
 */

package com.ibm.cohort.cli;

import com.ibm.cohort.annotations.Generated;
import com.ibm.cohort.tooling.fhir.ValueSetImporter;

@Generated
public class ValueSetImporterRunner implements ProgramRunner {
	@Override
	public void runProgram(String[] args) {
		try {
			ValueSetImporter.main(args);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to run MeasureImporter", e);
		}
	}
}
