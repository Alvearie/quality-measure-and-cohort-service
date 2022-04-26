/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import static com.ibm.cohort.cli.ParameterHelper.parseParameterArguments;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.ibm.cohort.cli.input.NoSplittingSplitter;
import com.ibm.cohort.cli.output.CqlEvaluationResultCollectionSizePrettyPrinter;
import com.ibm.cohort.cli.output.CqlEvaluationResultDisplayCollectionsPrettyPrinter;
import com.ibm.cohort.cli.output.CqlEvaluationResultPrettyPrinter;
import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.ContextNames;
import com.ibm.cohort.cql.evaluation.CqlDebug;
import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;
import com.ibm.cohort.cql.evaluation.CqlEvaluator;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.R4LibraryDependencyGatherer;
import com.ibm.cohort.cql.hapi.resolver.R4FhirServerResourceResolverFactory;
import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlVersionedIdentifier;
import com.ibm.cohort.cql.library.Format;
import com.ibm.cohort.cql.library.MapCqlLibraryProvider;
import com.ibm.cohort.cql.library.MapCqlLibraryProviderFactory;
import com.ibm.cohort.cql.library.PriorityCqlLibraryProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import com.ibm.cohort.engine.data.R4DataProviderFactory;
import com.ibm.cohort.engine.measure.cache.DefaultRetrieveCacheContext;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.r4.cache.R4FhirModelResolverFactory;
import com.ibm.cohort.engine.terminology.R4RestFhirTerminologyProvider;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class CohortCLI extends BaseCLI {

	public static final Format DEFAULT_SOURCE_FORMAT = Format.ELM;

	/**
	 * Command line argument definitions
	 */
	private static final class Arguments extends ConnectionArguments {
		@Parameter(names = { "-f",
				"--files" }, description = "Resource that contains the CQL library sources. Valid options are the path to a zip file or folder containing the cohort definitions or the resource ID of a FHIR Library resource contained in the measure server.", required = true)
		private String libraryPath;
		
		@Parameter(names = { "--filter" }, description = "Additional filters to apply to library loaders if supported by the library loading mechansim")
		private List<String> filters;

		@Parameter(names = { "-l",
				"--libraryName" }, description = "Library Name", required = true)
		private String libraryName;

		@Parameter(names = { "-v",
				"--libraryVersion" }, description = "Library Version", required = false)
		private String libraryVersion;

		@Parameter(names = { "-e", "--expression" }, description = "ELM Expression(s) to Execute", required = false)
		private Set<String> expressions;

		@Parameter(names = { "-c",
				"--context-id" }, description = "Unique ID for one or more context objects (e.g. Patient IDs)")
		private List<String> contextIds;

		@Parameter(names = { "-n",
				"--context-name" }, description = "Context Object Name")
		private String contextName = ContextNames.PATIENT;

		@Parameter(names = { "-p",
				"--parameters" }, description = "Parameter value(s) in format name:type:value where value can contain additional parameterized elements separated by comma. Multiple parameters must be specified as multiple -p options", splitter = NoSplittingSplitter.class, required = false)
		private List<String> parameters;

		@Parameter(names = { "-s",
				"--source-format" }, description = "Indicates which files in the file source should be processed", required = false)
		private Format sourceFormat = DEFAULT_SOURCE_FORMAT;
		
		@Parameter(names = { "-i",
				"--model-info" }, description = "Model info file used when translating CQL", required = false)
		private File modelInfoFile;

		@Parameter(names = {"--logging-level" }, description = "Specific logging level")
		private CqlDebug loggingLevel = CqlDebug.NONE;

		@Parameter(names = { "--enable-terminology-optimization" }, description = "By default, ValueSet resources used in CQL are first expanded by the terminology provider, then the codes are used to query the data server. If the data server contains the necessary terminology resources and supports the token :in search modifier, setting this flag to false will enable code filtering directly on the data server which should improve CQL engine throughput.", required = false )
		private boolean enableTerminologyOptimization = DEFAULT_TERMINOLOGY_OPTIMIZATION_ENABLED;
		
		@Parameter(names = { "--search-page-size" }, description = "Specifies how many records are requested per page during a FHIR search operation. The default value for servers can be quite small and setting this to a larger number will potentially improve performance.")
		private int searchPageSize = DEFAULT_PAGE_SIZE;

		@Parameter(names = { "--summarize-collections" }, description = "If set, collection sizes will be displayed in the CLI output instead of collection contents")
		private boolean isSummarizeCollections;
		
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
	 * @return CQLEvaluator
	 * @throws IOException IOException
	 */
	public CqlEvaluator runWithArgs(String[] args, PrintStream out) throws IOException {
		Arguments arguments = new Arguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("cql-engine").console(console).addObject(arguments).build();
		jc.parse(args);

		CqlEvaluator wrapper = null;
		
		if (arguments.isDisplayHelp) {
			jc.usage();
		} else {
			FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
			FhirClientBuilder fhirClientBuilder = factory.newFhirClientBuilder();
			CqlEvaluationResultPrettyPrinter prettyPrinter = arguments.isSummarizeCollections ? 
					new CqlEvaluationResultCollectionSizePrettyPrinter() : 
					new CqlEvaluationResultDisplayCollectionsPrettyPrinter();

			readConnectionConfiguration(arguments);

			MapCqlLibraryProviderFactory libraryProviderFactory = new MapCqlLibraryProviderFactory();

			String [] filters = null;
			if( arguments.filters != null ) {
				filters = arguments.filters.toArray(new String[arguments.filters.size()]);
			}

			CqlLibraryProvider backingLibraryProvider;
			Path libraryFolder = Paths.get(arguments.libraryPath);
			if (libraryFolder.toFile().isDirectory()) {
				out.println(String.format("Loading libraries from folder '%s'", libraryFolder.toString()));
				backingLibraryProvider = libraryProviderFactory.fromDirectory(libraryFolder, filters);
			} else if ( FileHelpers.isZip(libraryFolder.toFile()) ) {
				out.println(String.format("Loading libraries from ZIP '%s'", libraryFolder.toString()));
				backingLibraryProvider = libraryProviderFactory.fromZipFile(libraryFolder, filters);
			} else {
				out.println(String.format("Loading libraries from FHIR Library '%s'", libraryFolder.toString()));
				IGenericClient measureClient = fhirClientBuilder.createFhirClient(measureServerConfig);
				FhirResourceResolver<Library> libraryResolver = R4FhirServerResourceResolverFactory.createLibraryResolver(measureClient);
				R4LibraryDependencyGatherer dependencyGatherer = new R4LibraryDependencyGatherer(libraryResolver);
				List<Library> cqlLibraries = dependencyGatherer.gatherForLibraryId(arguments.libraryPath);
				Map<CqlLibraryDescriptor, CqlLibrary> cqlLibraryMap = toCqlLibraryMap(cqlLibraries);
				backingLibraryProvider = new MapCqlLibraryProvider(cqlLibraryMap);
			}

			CqlLibraryProvider fhirClasspathProvider = new ClasspathCqlLibraryProvider();
			backingLibraryProvider = new PriorityCqlLibraryProvider(backingLibraryProvider, fhirClasspathProvider);

			CqlToElmTranslator translator = new CqlToElmTranslator();
			if (arguments.modelInfoFile != null && arguments.modelInfoFile.exists()) {
				translator.registerModelInfo(arguments.modelInfoFile);
			}

			boolean isForceTranslation = arguments.sourceFormat == Format.CQL;
			CqlLibraryProvider libraryProvider = new TranslatingCqlLibraryProvider(backingLibraryProvider, translator, isForceTranslation);

			IGenericClient dataClient = fhirClientBuilder.createFhirClient(dataServerConfig);

			IGenericClient termClient = fhirClientBuilder.createFhirClient(terminologyServerConfig);
			CqlTerminologyProvider termProvider = new R4RestFhirTerminologyProvider(termClient);

			Map<String, com.ibm.cohort.cql.evaluation.parameters.Parameter> parameters = null;
			if (arguments.parameters != null) {
				parameters = parseParameterArguments(arguments.parameters);
			}

            CqlVersionedIdentifier libraryIdentifier = new CqlVersionedIdentifier(arguments.libraryName, arguments.libraryVersion);

			List<Pair<String, String>> contexts;
			if (arguments.contextIds == null || arguments.contextIds.isEmpty()) {
				// If no context ids are provided, perform one run using a null context
				contexts = Collections.singletonList(null);
			}
			else {
				contexts = arguments.contextIds.stream()
						.map(x -> new ImmutablePair<>(arguments.contextName, x))
						.collect(Collectors.toList());
			}

			try (RetrieveCacheContext cacheContext = new DefaultRetrieveCacheContext()) {
				CqlDataProvider dataProvider = R4DataProviderFactory.createDataProvider(
						dataClient,
						termProvider,
						cacheContext,
						R4FhirModelResolverFactory.createCachingResolver(),
						!arguments.enableTerminologyOptimization,
						arguments.searchPageSize
				);

				wrapper = new CqlEvaluator()
						.setLibraryProvider(libraryProvider)
						.setDataProvider(dataProvider)
						.setTerminologyProvider(termProvider);

				ZonedDateTime evaluationDateTime = ZonedDateTime.now();
				for (Pair<String, String> context : contexts) {
					String contextLabel = context == null ? "null" : context.getRight();
					out.println("Context: " + contextLabel);
					CqlEvaluationResult result = wrapper.evaluate(
							libraryIdentifier,
							parameters,
							context,
							arguments.expressions,
							arguments.loggingLevel,
							evaluationDateTime
					);

					out.print(prettyPrinter.prettyPrintResult(result));
					out.println("---");
				}
			}
		}
		return wrapper;
	}

	private Map<CqlLibraryDescriptor, CqlLibrary> toCqlLibraryMap(List<Library> libraries) {
		Map<CqlLibraryDescriptor, CqlLibrary> retVal = new HashMap<>();

		for (Library library : libraries) {
			String libraryId = library.getName();
			String version = library.getVersion();

			for (Attachment attachment : library.getContent()) {
				Format libraryFormat = Format.lookupByName(attachment.getContentType());
				if (libraryFormat != null) {
					CqlLibraryDescriptor key = new CqlLibraryDescriptor()
							.setLibraryId(libraryId)
							.setVersion(version)
							.setFormat(libraryFormat);
					CqlLibrary value = new CqlLibrary()
							.setContent(new String(attachment.getData()))
							.setDescriptor(key);
					retVal.put(key, value);
				}
			}
		}

		return retVal;
	}

	public static void main(String[] args) throws IOException {
		CohortCLI wrapper = new CohortCLI();
		wrapper.runWithArgs(args, System.out);
	}
}
