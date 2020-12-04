/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import static com.ibm.cohort.cli.ParameterHelper.parseParameterArguments;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.hl7.fhir.instance.model.api.IAnyResource;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.ibm.cohort.engine.CqlEngineWrapper;
import com.ibm.cohort.engine.DirectoryLibrarySourceProvider;
import com.ibm.cohort.engine.EvaluationResultCallback;
import com.ibm.cohort.engine.FhirClientBuilder;
import com.ibm.cohort.engine.FhirClientBuilderFactory;
import com.ibm.cohort.engine.FhirLibraryLibrarySourceProvider;
import com.ibm.cohort.engine.LibraryFormat;
import com.ibm.cohort.engine.MultiFormatLibrarySourceProvider;
import com.ibm.cohort.engine.TranslatingLibraryLoader;
import com.ibm.cohort.engine.ZipStreamLibrarySourceProvider;
import com.ibm.cohort.engine.translation.CqlTranslationProvider;
import com.ibm.cohort.engine.translation.InJVMCqlTranslationProvider;

public class CohortCLI extends BaseCLI {

	public static final LibraryFormat DEFAULT_SOURCE_FORMAT = LibraryFormat.XML;

	/**
	 * Command line argument definitions
	 */
	private static final class Arguments extends ConnectionArguments {
		@Parameter(names = { "-f",
				"--files" }, description = "Resource that contains the CQL library sources. Valid options are the path to a zip file or folder containing the cohort definitions or the resource ID of a FHIR Library resource contained in the measure server.", required = true)
		private String libraryPath;

		@Parameter(names = { "-l",
				"--libraryName" }, description = "Library Name", required = true)
		private String libraryName;

		@Parameter(names = { "-v",
				"--libraryVersion" }, description = "Library Version", required = false)
		private String libraryVersion;

		@Parameter(names = { "-e", "--expression" }, description = "ELM Expression(s) to Execute", required = false)
		private Set<String> expressions;

		@Parameter(names = { "-c",
				"--context-id" }, description = "Unique ID for one or more context objects (e.g. Patient IDs)", required = true)
		private List<String> contextIds;

		@Parameter(names = { "-p",
				"--parameters" }, description = "Parameter value(s) in format name:type:value where value can contain additional parameterized elements separated by comma", required = false)
		private List<String> parameters;

		@Parameter(names = { "-s",
				"--source-format" }, description = "Indicates which files in the file source should be processed", required = false)
		private LibraryFormat sourceFormat = DEFAULT_SOURCE_FORMAT;
		
		@Parameter(names = { "-i",
		"--model-info" }, description = "Model info file used when translating CQL", required = false)
		private File modelInfoFile;

		@Parameter(names = { "-h", "--help" }, description = "Display this help", required = false, help = true)
		private boolean isDisplayHelp;
	}



	/**
	 * Simulate main method behavior in a non-static context for use in testing
	 * tools. This method is intended to be called only once. Multiple calls for the
	 * same library path will attempt duplicate library loading.
	 * 
	 * @param args parameter values
	 * @param out  location where contents that would normally go to stdout should
	 *             be written
	 * @throws Exception
	 */
	public CqlEngineWrapper runWithArgs(String[] args, PrintStream out) throws Exception {
		Arguments arguments = new Arguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("cql-engine").console(console).addObject(arguments).build();
		jc.parse(args);

		CqlEngineWrapper wrapper = null;
		
		if (arguments.isDisplayHelp) {
			jc.usage();
		} else {
			
			FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
			FhirClientBuilder builder = factory.newFhirClientBuilder();
			
			wrapper = new CqlEngineWrapper(builder);

			configureConnections(wrapper, arguments);

			Path libraryFolder = Paths.get(arguments.libraryPath);
			MultiFormatLibrarySourceProvider sourceProvider = null;
			if (libraryFolder.toFile().isDirectory()) {
				out.println(String.format("Loading libraries from folder '%s'", libraryFolder.toString()));
				sourceProvider = new DirectoryLibrarySourceProvider(libraryFolder);
			} else if (libraryFolder.toFile().isFile() && libraryFolder.toString().endsWith(".zip")) {
				out.println(String.format("Loading libraries from ZIP '%s'", libraryFolder.toString()));
				try (InputStream is = new FileInputStream(libraryFolder.toFile())) {
					sourceProvider = new ZipStreamLibrarySourceProvider(new ZipInputStream(is));
				}
			} else {
				out.println(String.format("Loading libraries from FHIR Library '%s'", libraryFolder.toString()));
				sourceProvider = new FhirLibraryLibrarySourceProvider(wrapper.getMeasureServerClient(), arguments.libraryPath);
			}

			boolean isForceTranslation = arguments.sourceFormat == LibraryFormat.CQL;
			CqlTranslationProvider translationProvider = new InJVMCqlTranslationProvider(sourceProvider);
			if (arguments.modelInfoFile != null && arguments.modelInfoFile.exists()) {
				translationProvider.convertAndRegisterModelInfo(arguments.modelInfoFile);
			}
			wrapper.setLibraryLoader(new TranslatingLibraryLoader(sourceProvider, translationProvider, isForceTranslation));

			Map<String, Object> parameters = null;
			if (arguments.parameters != null) {
				parameters = parseParameterArguments(arguments.parameters);
			}
			
			wrapper.evaluate(arguments.libraryName, arguments.libraryVersion, parameters, arguments.expressions,
					arguments.contextIds, new EvaluationResultCallback() {

						@Override
						public void onContextBegin(String contextId) {
							out.println("Context: " + contextId);
						}

						@Override
						public void onEvaluationComplete(String contextId, String expression, Object result) {
						
							String value;
							if( result != null ) {
								if( result instanceof IAnyResource ) {
									IAnyResource resource = (IAnyResource) result;
									value = resource.getId();
								} else if( result instanceof Collection ) {
									Collection<?> collection = (Collection<?>) result;
									value = "Collection: " + collection.size();
								} else {
									value = result.toString();
								}
							} else {
								value = "null";
							}
							
							out.println(String.format("Expression: \"%s\", Result: %s", expression, value));
						}

						@Override
						public void onContextComplete(String contextId) {
							out.println("---");
						}
					});
		}
		return wrapper;
	}

	protected void configureConnections(CqlEngineWrapper wrapper, ConnectionArguments arguments)
			throws Exception {
		readConnectionConfiguration(arguments);
		wrapper.setDataServerConnectionProperties(dataServerConfig);
		wrapper.setTerminologyServerConnectionProperties(terminologyServerConfig);
		wrapper.setMeasureServerConnectionProperties(measureServerConfig);
	}

	public static void main(String[] args) throws Exception {
		CohortCLI wrapper = new CohortCLI();
		wrapper.runWithArgs(args, System.out);
	}
}
