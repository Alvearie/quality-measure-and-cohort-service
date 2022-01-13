package com.ibm.cohort.cli;

import com.ibm.cohort.annotations.Generated;
import com.ibm.cohort.tooling.fhir.MeasureImporter;

@Generated
public class MeasureImporterRunner implements ProgramRunner {
	@Override
	public void runProgram(String[] args) {
		try {
			MeasureImporter.main(args);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to run MeasureImporter", e);
		}
	}
}
