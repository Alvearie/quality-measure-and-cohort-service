package com.ibm.cohort.cli;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FhirResourceToolingCLIDriver extends CLIDriver {
	protected static final String MEASURE_IMPORTER = "measure-importer";
	protected static final String VALUE_SET_IMPORTER = "value-set-importer";
	
	private static final Set<String> VALID_COMMANDS = new HashSet<>(Arrays.asList(MEASURE_IMPORTER, VALUE_SET_IMPORTER));

	public static void main(String[] args) {
		CLIDriver cliDriver = new FhirResourceToolingCLIDriver();
		cliDriver.runProgram(args);
	}

	@Override
	public ProgramRunner getRunnableProgram(String command) throws UnsupportedOperationException {
		if (command.equals(MEASURE_IMPORTER)) {
			return new MeasureImporterRunner();
		}
		else if (command.equals(VALUE_SET_IMPORTER)) {
			return new ValueSetImporterRunner();
		}
		else {
			throw new UnsupportedOperationException(getStandardCommandError(command));
		}
	}

	@Override
	public Set<String> getValidCommands() {
		return VALID_COMMANDS;
	}
}
