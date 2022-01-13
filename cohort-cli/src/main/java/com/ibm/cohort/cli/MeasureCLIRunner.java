package com.ibm.cohort.cli;

import com.ibm.cohort.annotations.Generated;

@Generated
public class MeasureCLIRunner implements ProgramRunner {
	@Override
	public void runProgram(String[] args) {
		try {
			MeasureCLI.main(args);
		} catch (Exception e) {
			throw new RuntimeException("Failed to run MeasureCLI", e);
		}
	}
}
