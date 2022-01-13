package com.ibm.cohort.cli;

import com.ibm.cohort.tooling.fhir.ValueSetImporter;

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
