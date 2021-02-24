/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.OperationOutcome;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

/**
 * Load a FHIR server with resources provided in the UI measure export format.
 * All resources contained in a UI export artifact are uploaded in a single
 * transaction. If the transaction fails, then an HTTP 400 or 500 response will
 * be generated and the OperationOutcome will describe the reason for failure.
 * Otherwise, the response will be 200 and the contents of the response will
 * describe what changes were made. See
 * <a href="https://www.hl7.org/fhir/http.html#trules">the fhir
 * specification</a> for complete details.
 *
 * Pre-reqs: The FHIR server uses SSL by default and you must trust the FHIR
 * server's public key in order to perform operations against the server. One
 * way you could do this is shown below with the assumption that you have the
 * FHIR server source code loaded locally. There are many other ways including
 * using openssl s_client to capture the cert from a running FHIR server.
 * 
 * $&lt; keytool -importkeystore -srckeystore
 * /path/to/git/FHIR/fhir-server/liberty-config/resources/security/fhirKeyStore.p12
 * -srcstorepass change-password -cacerts -deststorepass changeit
 */
public class MeasureImporter {
	private static final String FHIR_RESOURCES = "fhirResources/";
	private static final String JSON_EXT = ".json";
	private IdStrategy idStrategy = null;

	/**
	 * Wrapper for command-line arguments.
	 */
	public static final class Arguments {
		@Parameter(names = { "-m",
				"--measure-server" }, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve measure and library resources.", required = true)
		File measureServerConfigFile;

		@Parameter(names = { "-o",
				"--output-path" }, description = "Path to a folder where output will be written. If not provided, then output is written to the current working directory.", required = false)
		String outputPath = ".";

		@Parameter(names = { "-h", "--help" }, description = "Show this help", help = true)
		boolean isDisplayHelp;

		@Parameter(description = "<zip> [<zip> ...]", required = true)
		List<String> artifactPaths;
	}

	private IGenericClient client;
	private IParser parser;

	public MeasureImporter(IGenericClient client) {
		this(client, new SHA256NameVersionIdStrategy());
	}

	public MeasureImporter(IGenericClient client, IdStrategy idStrategy) {
		this.client = client;
		this.parser = client.getFhirContext().newJsonParser();

		this.idStrategy = idStrategy;
	}

	/**
	 * Given a ZIP artifact convert the contents into a FHIR bundle
	 * 
	 * @param is InputStream containing artifacts to import
	 * @return FHIR bundle
	 * @throws Exception any error
	 */
	public Bundle convertToBundle(InputStream is) throws Exception {

		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;

		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.TRANSACTION);

		while ((entry = zis.getNextEntry()) != null) {
			if (isDeployable(entry)) {
				IBaseResource resource = parser.parseResource(zis);
				if (resource instanceof MetadataResource) {
					MetadataResource metadataResource = (MetadataResource) resource;

					if (metadataResource.hasName() && metadataResource.hasUrl() && metadataResource.hasVersion()) {
						if (!metadataResource.hasId()) {
							metadataResource.setId(idStrategy.generateId(metadataResource));
						}

						String url = metadataResource.fhirType() + "/" + metadataResource.getId();

						bundle.addEntry().setFullUrl(url).setResource(metadataResource).getRequest().setUrl(url)
								.setMethod(Bundle.HTTPVerb.PUT);
					} else {
						throw new IllegalArgumentException(
								"Knowledge resources must specify at least name, version, and url");
					}
				}

			}
		}

		return bundle;
	}

	/**
	 * Check whether ZipEntry represents a resource that should be deployed to the
	 * FHIR server.
	 * 
	 * @param entry metadata about the zip file entry
	 * @return true if entry is deployable or false otherwise
	 */
	public static boolean isDeployable(ZipEntry entry) {
		return entry.getName().startsWith(FHIR_RESOURCES) && entry.getName().endsWith(JSON_EXT);
	}

	/**
	 * Import the contents of a list of UI export files.
	 * 
	 * @param artifactPaths list of UI export file paths.
	 * @return number representing how many errors were encountered processing the 
	 * specified artifacts
	 * @throws Exception on any error.
	 */
	public int importFiles(List<String> artifactPaths, String outputPath) throws Exception {
		int errorCount = 0;
		for (String artifactPath : artifactPaths) {
			Pair<Bundle, IBaseResource> details = importFile(artifactPath, outputPath);
			if( details.getRight() instanceof OperationOutcome ) {
				errorCount = errorCount + 1;
			}
		}
		return errorCount;
	}

	/**
	 * Import the contents of a single UI export file.
	 * 
	 * @param path path to the UI export file.
	 * @return Pair of FHIR resources representing the request bundle and server response (bundle, operation outcome, etc.)
	 * @throws Exception on any error
	 */
	public Pair<Bundle, IBaseResource> importFile(String inputPath, String output) throws Exception {
		Bundle request;
		try (InputStream is = new FileInputStream(inputPath)) {
			request = convertToBundle(is);
		}

		Path outputPath = Paths.get(output);

		Path requestPath = outputPath.resolve(FilenameUtils.getBaseName(inputPath) + "-request.json");
		Files.write(requestPath, parser.encodeResourceToString(request).getBytes(StandardCharsets.UTF_8));

		IBaseResource response = null;
		try {
			response = client.transaction().withBundle(request).execute();
		} catch( BaseServerResponseException ex ) {
			if( ex.getOperationOutcome() != null ) {
				response = ex.getOperationOutcome();
			}
		}

		if( response != null ) {
			Path responsePath = outputPath.resolve(FilenameUtils.getBaseName(inputPath) + "-response.json");
			Files.write(responsePath, parser.encodeResourceToString(response).getBytes(StandardCharsets.UTF_8));
		}

		return Pair.of(request, response);
	}

	/**
	 * Execute measure import process with given arguments. Console logging is
	 * redirected to the provided stream.
	 * 
	 * @param args Program arguments
	 * @param out  Sink for console output
	 * @return number of errors encountered during processing
	 * @throws Exception on any error.
	 */
	public static int runWithArgs(String[] args, PrintStream out) throws Exception {
		int resultCode = 0;
		
		Arguments arguments = new Arguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("measure-importer").console(console).addObject(arguments)
				.build();
		jc.parse(args);

		if (arguments.isDisplayHelp) {
			jc.usage();
			resultCode = 1;
		} else {
			FhirContext fhirContext = FhirContext.forR4();

			ObjectMapper om = new ObjectMapper();
			FhirServerConfig config = om.readValue(arguments.measureServerConfigFile, FhirServerConfig.class);
			IGenericClient client = FhirClientBuilderFactory.newInstance().newFhirClientBuilder(fhirContext)
					.createFhirClient(config);

			MeasureImporter importer = new MeasureImporter(client);
			resultCode = importer.importFiles(arguments.artifactPaths, arguments.outputPath);
		}
		
		return resultCode;
	}

	public static void main(String[] args) throws Exception {
		System.exit( MeasureImporter.runWithArgs(args, System.out) );
	}
}
