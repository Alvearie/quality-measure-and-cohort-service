/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(ValueSetImporter.class.getName());
	public static final class ValueSetImporterArguments {
		@Parameter(names = {"-m",
				"--measure-server"}, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve measure and library resources.")
		File measureServerConfigFile;

		@Parameter(names = {"--override-existing-value-sets"}, description = "Will force insertion of valuesets, even if they already exist")
		boolean overrideValueSets;

		@Parameter(names = {"-c", "--code-system-mappings"}, description = "Custom code system mappings")
		String filename;

		@Parameter(names = {"-h", "--help"}, description = "Show this help", help = true)
		boolean isDisplayHelp;
		
		@Parameter(names = {"-p", "--file-system-output-path"}, description = "If specified, value sets will be converted to FHIR format and writen out to the file system path specified by this parameter. Value sets will not be imported into the FHIR server if this option is set.", required = false)
		String fileSystemOutputPath;
		
		@Parameter(names = {"-o", "--file-system-output-format"}, description = "Format to use when exporting value sets to the file system when using the -p/--file-system-output-path parameters. Valid values are JSON or XML. If not specified, the default output format will be JSON", required = false)
		String filesystemOutputFormat = "json";

		@Parameter(description = "The list of value set set spreadsheets to import", required = true)
		List<String> spreadsheets;
		
		public void validate() {
			//check to make sure we are either exporting to file OR importing to a fhir server, but not both
			if(fileSystemOutputPath != null && measureServerConfigFile != null) {
				throw new IllegalArgumentException("Parameters [-m, --measure-server] and [-p, --file-system-output-path] cannot both be specified on the same invocation");
			}
			
			if(fileSystemOutputPath == null && measureServerConfigFile == null) {
				throw new IllegalArgumentException("Parameters [-m, --measure-server] and [-p, --file-system-output-path] cannot both be null. Please supply a value for one of these parameters");
			}
			
			//check for valid file system output format
			if (filesystemOutputFormat != null && !(filesystemOutputFormat.equalsIgnoreCase("json") || filesystemOutputFormat.equalsIgnoreCase("xml"))){
				throw new IllegalArgumentException("Bad parameter value. Valid values for [-o, --file-system-output-format parameter] are json or xml");
			}
		}
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
			arguments.validate();
			FhirContext fhirContext = FhirContext.forR4();

			
			ObjectMapper om = new ObjectMapper();
			IGenericClient client = null;
			//only connect to fhir server if we are not writing it to file system
			if(arguments.fileSystemOutputPath == null) {
				FhirServerConfig config = om.readValue(arguments.measureServerConfigFile, FhirServerConfig.class);
				client = FhirClientBuilderFactory.newInstance().newFhirClientBuilder(fhirContext)
						.createFhirClient(config);
			}
			
			Map<String, String> codeSystemMappings = null;
			if(arguments.filename != null) {
				codeSystemMappings = ValueSetUtil.getMapFromInputStream(new FileInputStream(new File(arguments.filename)));
			}

			for (String arg : arguments.spreadsheets) {
				try (InputStream is = new FileInputStream(arg)) {
					ValueSetArtifact artifact = ValueSetUtil.createArtifact(is, codeSystemMappings);
					
					//only import the value set to fhir server if we are not writing the value set to file system
					if(arguments.fileSystemOutputPath == null) {
						String retVal = ValueSetUtil.importArtifact(client, artifact, arguments.overrideValueSets);
						if(retVal == null){
							logger.error("Value set already exists! Please provide the override option if you would like to override this value set.");
						}
					}else {
						//write value set to file system
						ValueSet vs = artifact.getFhirResource();
						
						//If the valueset id contains urn:oid, remove it to make a valid filename
						String valueSetId = vs.getId().startsWith("urn:oid:") ? vs.getId().replace("urn:oid:", "") : vs.getId();						
						String vsFileName = valueSetId + "." +arguments.filesystemOutputFormat.toLowerCase();
						
						//create the output dir if it doesn't exist
						File outputDir = new File(arguments.fileSystemOutputPath);
						if(!outputDir.exists()) {
							outputDir.mkdir();
						}
						
						//write to xml or json format
						BufferedWriter writer = new BufferedWriter(new FileWriter(arguments.fileSystemOutputPath + System.getProperty("file.separator")+vsFileName));
						if(arguments.filesystemOutputFormat == null || arguments.filesystemOutputFormat.isEmpty() || arguments.filesystemOutputFormat.equalsIgnoreCase("json")) {
							fhirContext.newJsonParser().encodeResourceToWriter(vs, writer);
						}else if (arguments.filesystemOutputFormat.equalsIgnoreCase("xml")) {
							fhirContext.newXmlParser().encodeResourceToWriter(vs, writer);
						}
						writer.close();
					}
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ValueSetImporter.runWithArgs(args, System.out);
	}
}


