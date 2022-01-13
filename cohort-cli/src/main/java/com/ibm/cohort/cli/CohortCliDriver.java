package com.ibm.cohort.cli;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CohortCliDriver extends BaseCLIDriver {
	protected static final String COHORT_CLI = "cohort-cli";
	protected static final String MEASURE_CLI = "measure-cli";
	protected static final String TRANSLATION_CLI = "translation-cli";

	private static final Set<String> VALID_COMMANDS = new HashSet<>(Arrays.asList(COHORT_CLI, MEASURE_CLI, TRANSLATION_CLI));

	public static void main(String[] args) {
		BaseCLIDriver cliDriver = new CohortCliDriver();
		cliDriver.runProgram(args);
	}

	@Override
	public ProgramRunner getRunnableProgram(String command) throws UnsupportedOperationException {
		if (command.equals(COHORT_CLI)) {
			return new CohortCLIRunner();
		}
		else if (command.equals(MEASURE_CLI)) {
			return new MeasureCLIRunner();
		}
		else if (command.equals(TRANSLATION_CLI)) {
			return new TranslationCLIRunner();
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
