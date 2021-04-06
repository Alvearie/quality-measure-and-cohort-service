/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.cohort.valueset.ValueSetArtifact;
import com.ibm.cohort.valueset.ValueSetUtil;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class ValueSetImporter {

	public static final class ValueSetImporterArguments {
		@Parameter(names = {"-m",
				"--measure-server"}, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve measure and library resources.", required = true)
		File measureServerConfigFile;

		@Parameter(names = {"--override-existing-value-sets"}, description = "Will force insertion of valuesets, even if they already exist")
		boolean overrideValueSets;

		@Parameter(names = {"-c, --code-system-mappings"}, description = "Custom code system mappings")
		String filename;

		@Parameter(names = {"-h", "--help"}, description = "Show this help", help = true)
		boolean isDisplayHelp;

		@Parameter(description = "The list of value set set spreadsheets to import", required = true)
		List<String> spreadsheets;
	}

	static void runWithArgs(String[] args, PrintStream out) throws IOException {
		ValueSetImporterArguments arguments = new ValueSetImporterArguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("value-set-importer").console(console).addObject(arguments)
				.build();
		jc.parse(args);

		if (arguments.isDisplayHelp) {
			jc.usage();
		} else {
			FhirContext fhirContext = FhirContext.forR4();

			ObjectMapper om = new ObjectMapper();
			FhirServerConfig config = om.readValue(arguments.measureServerConfigFile, FhirServerConfig.class);
			IGenericClient client = FhirClientBuilderFactory.newInstance().newFhirClientBuilder(fhirContext)
					.createFhirClient(config);

			for (String arg : arguments.spreadsheets) {
				try (InputStream is = new FileInputStream(arg)) {
					ValueSetArtifact artifact = ValueSetUtil.createArtifact(is);
					ValueSetUtil.importArtifact(client, artifact, arguments.overrideValueSets);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ValueSetImporter.runWithArgs(args, System.out);
	}
}
