package com.ibm.cohort.cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.annotations.Generated;

@Generated
public class CLIDriver {
	private static final Logger LOGGER = LoggerFactory.getLogger(CLIDriver.class);
	
	public void runProgram(String[] args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("Must provide a command to run.");
		}
		try {
			ProgramRunner programRunner = getRunnableProgram(args[0]);
			programRunner.runProgram(Arrays.copyOfRange(args, 1, args.length));
		}
		catch (UnsupportedOperationException e) {
			Set<String> validCommands = getValidCommands();
			if (CollectionUtils.isNotEmpty(validCommands)) {
				LOGGER.error("Valid commands are: {}", String.join(", ", validCommands));
			}
			throw e;
		}
	}
	
	public ProgramRunner getRunnableProgram(String command) throws UnsupportedOperationException {
		return null;
	}
	
	public Set<String> getValidCommands() {
		return Collections.emptySet();
	}
	
	public String getStandardCommandError(String command) throws UnsupportedOperationException {
		return "No such command '" + command + "'";
	}
}
