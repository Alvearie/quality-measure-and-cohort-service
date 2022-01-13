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
