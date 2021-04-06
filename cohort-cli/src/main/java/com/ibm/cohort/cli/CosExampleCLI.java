/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cli;

import java.io.PrintStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.beust.jcommander.internal.DefaultConverterFactory;
import com.ibm.cohort.cli.converter.json.JsonFileConverterInstanceFactory;
import com.ibm.cohort.engine.cos.CosConfiguration;
import com.ibm.cohort.engine.cos.CosDao;
import com.ibm.cohort.engine.cos.CosDaoFactory;

public class CosExampleCLI {
	private static final Logger logger = LoggerFactory.getLogger(CosExampleCLI.class);

	private CosExampleCLI() {
	}

	public static void main(String[] args) {
		CosExampleCLI cli = new CosExampleCLI();
		cli.runWithArgs(args, System.out);

		System.exit(0);
	}

	private void runWithArgs(String[] args, PrintStream out) {
		CosExampleWrapper argsObject = new CosExampleWrapper();
		Console console = new DefaultConsole(out);
		JCommander jc =
				JCommander.newBuilder()
						.programName("cos-example")
						.console(console)
						.addObject(argsObject)
						.addConverterInstanceFactory(new JsonFileConverterInstanceFactory())
						.addConverterFactory(new DefaultConverterFactory())
						.build();

		jc.parse(args);

		CosDao cosDao = CosDaoFactory.from(argsObject.getCosConfiguration());

		List<String> buckets = cosDao.getBuckets();
		logger.info("Listing of COS buckets: {}", String.join(", ", buckets));
	}

	private static class CosExampleWrapper {
		@Parameter(names = {"--cos-config"}, description = "Path to COS configuration file (json format)")
		private CosConfiguration cosConfiguration;

		CosExampleWrapper() {
		}

		CosConfiguration getCosConfiguration() {
			return cosConfiguration;
		}
	}
}
