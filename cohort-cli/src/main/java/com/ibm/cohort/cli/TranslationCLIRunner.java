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
