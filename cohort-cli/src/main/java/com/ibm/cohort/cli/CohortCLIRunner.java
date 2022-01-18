/*
 * (C) Copyright IBM Corp. 2022, 2022
 *  
 * SPDX-License-Identifier: Apache-2.0
 *  
 */
package com.ibm.cohort.cli;

import com.ibm.cohort.annotations.Generated;

@Generated
public class CohortCLIRunner implements ProgramRunner {
	@Override
	public void runProgram(String[] args) {
		try {
			CohortCLI.main(args);
		} catch (Exception e) {
			throw new RuntimeException("Failed to run CohortCLI", e);
		}
	}
}
