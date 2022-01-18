/*
 * (C) Copyright IBM Corp. 2022, 2022
 *  
 * SPDX-License-Identifier: Apache-2.0
 *  
 */

package com.ibm.cohort.cli;

import com.ibm.cohort.annotations.Generated;

@Generated
public class TranslationCLIRunner implements ProgramRunner {
	@Override
	public void runProgram(String[] args) {
		try {
			TranslationCLI.main(args);
		} catch (Exception e) {
			throw new RuntimeException("Failed to run TranslationCLI", e);
		}
	}
}
