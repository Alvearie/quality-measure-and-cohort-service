package com.ibm.cohort.cli;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CohortCliCLIDriver extends CLIDriver{
	private static final String COHORT_CLI = "cohort-cli";
	private static final String MEASURE_CLI = "measure-cli";
	private static final String TRANSLATION_CLI = "translation-cli";

	private static final Set<String> VALID_COMMANDS = new HashSet<>(Arrays.asList(COHORT_CLI, MEASURE_CLI, TRANSLATION_CLI));

	public static void main(String[] args) {
		CLIDriver cliDriver = new CohortCliCLIDriver();
		cliDriver.runProgram(args);
	}

	@Override
	public RunnableProgram getRunnableProgram(String command) throws UnsupportedOperationException {
		if (command.equals(COHORT_CLI)) {
			return new CohortCLI();
		}
		else if (command.equals(MEASURE_CLI)) {
			return new MeasureCLI();
		}
		else if (command.equals(TRANSLATION_CLI)) {
			return new TranslationCLI();
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
