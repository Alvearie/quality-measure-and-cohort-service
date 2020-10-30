/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.elm.execution.ExpressionDef;
import org.cqframework.cql.elm.execution.FunctionDef;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.ParameterDef;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.retrieve.RestFhirRetrieveProvider;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.fhir.terminology.R4FhirTerminologyProvider;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Time;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Provide a wrapper around the open source reference implementation of a CQL
 * engine using sensible defaults for our target execution environment and
 * providing utilities for loading library contents from various sources.
 */
public class CqlEngineWrapper {
	
	public static final List<String> SUPPORTED_MODELS = Arrays.asList(
		"http://hl7.org/fhir",
		"http://hl7.org/fhir/us/core",
		"http://hl7.org/fhir/us/qicore",
		"http://ibm.com/fhir/cdm"
	);

	private static final LibraryFormat DEFAULT_SOURCE_FORMAT = LibraryFormat.XML;

	private static Logger LOG = LoggerFactory.getLogger(CqlEngineWrapper.class);

	private List<Library> libraries = new ArrayList<>();

	private FhirClientFactory clientFactory;

	private IGenericClient dataServerClient;
	private IGenericClient measureServerClient;
	private IGenericClient terminologyServerClient;

	public CqlEngineWrapper() {
		this(FhirClientFactory.newInstance(FhirContext.forR4()));
	}

	public CqlEngineWrapper(FhirClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * Add library content to the list of libraries available to the CQL engine at
	 * runtime.
	 * 
	 * @param libraryData  Data representing the library content. Can be CQL or ELM.
	 * @param sourceFormat LibrayFormat indicating which source files and format to
	 *                     read from the ZIP stream
	 * @param provider     When <code>sourceFormat</code> is CQL, provider is
	 *                     required and is an interface the allows the translator to
	 *                     load library includes. The provider implementation
	 *                     differs based on where the source data is coming from. If
	 *                     sourceFormat is not CQL, this can safely be null.
	 * @throws Exception
	 */
	public void addLibrary(InputStream libraryData, LibraryFormat sourceFormat, LibrarySourceProvider provider)
			throws Exception {
		switch (sourceFormat) {
		case CQL:
			InJVMCqlTranslationProvider translator = new InJVMCqlTranslationProvider();
			translator.addLibrarySourceProvider(provider);
			libraries.add(translator.translate(libraryData));
			break;
		case XML:
			libraries.add(CqlLibraryReader.read(libraryData));
			break;
//TODO: uncomment when JSON deserialization issues are addressed
//		case JSON:
//			libraries.add(JsonCqlLibraryReader.read(new InputStreamReader(libraryData)));
//			break;
		default:
			throw new IllegalArgumentException("Unsupported library format");
		}
	}

	/**
	 * Add all CQL libraries in path pointing to a directory using the default
	 * source format as input.
	 * 
	 * @param libraryFolder path to a folder
	 * @return number of libraries loaded
	 * @throws Exception
	 */
	public int addLibrariesFromZip(Path libraryFolder) throws Exception {
		return addLibrariesFromZip(libraryFolder, DEFAULT_SOURCE_FORMAT);
	}

	/**
	 * Add all CQL libraries in path pointing to a ZIP file
	 * 
	 * @param zipFilePath  path to a ZIP file
	 * @param sourceFormat LibrayFormat indicating which source files and format to
	 *                     read from the ZIP stream
	 * @return number of libraries loaded
	 */
	public int addLibrariesFromZip(Path zipFilePath, LibraryFormat sourceFormat) throws Exception {
		int numLoaded = 0;
		try (InputStream is = new FileInputStream(zipFilePath.toFile())) {
			numLoaded = addLibrariesFromZipStream(is, sourceFormat);
		}
		return numLoaded;
	}

	/**
	 * Add all CQL libraries in provided input stream
	 * 
	 * @param is input stream containing ZIP contents. Callers are responsible for
	 *           closing the stream when they are done with it.
	 * @return number of libraries loaded
	 */
	public int addLibrariesFromZipStream(InputStream is) throws Exception {
		return addLibrariesFromZipStream(is, DEFAULT_SOURCE_FORMAT);
	}

	/**
	 * Add all CQL libraries in provided input stream
	 * 
	 * @param is           input stream containing ZIP contents. Callers are
	 *                     responsible for closing the stream when they are done
	 *                     with it.
	 * @param sourceFormat LibrayFormat indicating which source files and format to
	 *                     read from the ZIP stream
	 * @return number of libraries loaded
	 */
	public int addLibrariesFromZipStream(InputStream is, LibraryFormat sourceFormat) throws Exception {
		ZipInputStream zis = new ZipInputStream(is);
		ZipStreamLibrarySourceProvider provider = new ZipStreamLibrarySourceProvider(zis);

		return addLibraries(provider, sourceFormat);
	}

	/**
	 * Add all CQL libraries in path pointing to a filesystem folder
	 * 
	 * @param libraryFolder path to a folder
	 * @return number of libraries loaded
	 */
	public int addLibrariesFromFolder(Path libraryFolder) throws Exception {
		return addLibrariesFromFolder(libraryFolder, DEFAULT_SOURCE_FORMAT);
	}

	/**
	 * Add all CQL libraries in path pointing to a filesystem folder
	 * 
	 * @param libraryFolder path to a folder containing files of the specified
	 *                      <code>sourceFormat</code>
	 * @param sourceFormat  LibrayFormat indicating which source files and format to
	 *                      read from the ZIP stream
	 * @return number of libraries loaded
	 */
	public int addLibrariesFromFolder(Path libraryFolder, LibraryFormat sourceFormat) throws Exception {
		MultiFormatLibrarySourceProvider provider = new DirectoryLibrarySourceProvider(libraryFolder);
		return addLibraries(provider, sourceFormat);
	}

	/**
	 * Add all CQL Libraries from the target FHIR Library including any
	 * dependencies.
	 * 
	 * @param libraryId    resource ID of a FHIR Library resource
	 * @param sourceFormat LibrayFormat indicating which source files and format to
	 *                     read from the FHIR Library resource
	 * @return number of libraries loaded
	 */
	public int addLibrariesFromFhirResource(String libraryId, LibraryFormat sourceFormat) throws Exception {
		org.hl7.fhir.r4.model.Library library = measureServerClient.read().resource(org.hl7.fhir.r4.model.Library.class)
				.withId(libraryId).execute();

		return addLibrariesFromFhirResource(measureServerClient, library, sourceFormat);
	}

	/**
	 * Add all CQL Libraries from the provided FHIR Library including any
	 * dependencies.
	 * 
	 * @param library      FHIR Library resource
	 * @param sourceFormat LibrayFormat indicating which source files and format to
	 *                     read from the FHIR Library resource
	 * @return number of libraries loaded
	 */
	public int addLibrariesFromFhirResource(org.hl7.fhir.r4.model.Library library, LibraryFormat sourceFormat)
			throws Exception {
		return addLibrariesFromFhirResource(measureServerClient, library, sourceFormat);
	}

	/**
	 * Add all CQL Libraries from the provided FHIR Library including any
	 * dependencies.
	 * 
	 * @param fhirClient   HAPI FHIR client that has been configured to interact
	 *                     with the FHIR server containing the Library resources of
	 *                     interest.
	 * @param library      FHIR Library resource
	 * @param sourceFormat LibrayFormat indicating which source files and format to
	 *                     read from the FHIR Library resource
	 * @return number of libraries loaded
	 */
	public int addLibrariesFromFhirResource(IGenericClient fhirClient, org.hl7.fhir.r4.model.Library library,
			LibraryFormat sourceFormat) throws Exception {
		MultiFormatLibrarySourceProvider provider = new FhirLibraryLibrarySourceProvider(fhirClient, library);

		return addLibraries(provider, sourceFormat);
	}

	/**
	 * Add all CQL Libraries from the provided source collection
	 * 
	 * @param provider     MultiSource collection of library resources
	 * @param sourceFormat LibrayFormat indicating which source files and format to
	 *                     read from the FHIR Library resource
	 * @return number of libraries loaded
	 */
	public int addLibraries(MultiFormatLibrarySourceProvider provider, LibraryFormat sourceFormat) throws Exception {
		int numLoaded = 0;
		Map<org.hl7.elm.r1.VersionedIdentifier, InputStream> sources = provider.getSourcesByFormat(sourceFormat);
		for (InputStream is : sources.values()) {
			this.addLibrary(is, sourceFormat, provider);
			numLoaded++;
		}
		LOG.info("Loaded {} libraries", numLoaded);
		return numLoaded;
	}

	/**
	 * Configure the FHIR server used for retrieve operations.
	 * 
	 * @param config
	 */
	public void setDataServerConnectionProperties(FhirServerConfig config) {
		setDataServerClient(clientFactory.createFhirClient(config));
	}

	/**
	 * Set the FHIR client used for data operations.
	 * 
	 * @param config
	 */
	public void setDataServerClient(IGenericClient client) {
		this.dataServerClient = client;
	}

	/**
	 * Get the data server client object
	 * 
	 * @return data server client
	 */
	public IGenericClient getDataServerClient() {
		return this.dataServerClient;
	}

	/**
	 * Configure the FHIR server used for quality measure operations.
	 * 
	 * @param config
	 */
	public void setMeasureServerConnectionProperties(FhirServerConfig config) {
		setMeasureServerClient(clientFactory.createFhirClient(config));
	}

	/**
	 * Set the FHIR client used to interact with the FHIR measure server.
	 * 
	 * @param client
	 */
	public void setMeasureServerClient(IGenericClient client) {
		this.measureServerClient = client;
	}

	/**
	 * Get the measure server client.
	 * 
	 * @return measure server client.
	 */
	public IGenericClient getMeasureServerClient() {
		return this.measureServerClient;
	}

	/**
	 * Configure the FHIR server used for terminology operations.
	 * 
	 * @param config
	 */
	public void setTerminologyServerConnectionProperties(FhirServerConfig config) {
		this.terminologyServerClient = clientFactory.createFhirClient(config);
	}

	/**
	 * Set the FHIR client used for terminology data access.
	 * 
	 * @param client
	 */
	public void setTerminologyServerClient(IGenericClient client) {
		this.terminologyServerClient = client;
	}

	/**
	 * Get the terminology server client.
	 * 
	 * @return terminology server client.
	 */
	public IGenericClient getTerminologyServerClient() {
		return this.terminologyServerClient;
	}

	/**
	 * Usage pattern of CQL Engine based on the Executor class in the
	 * cql_execution_service. This is an amount of detail that should be handled by
	 * the CqlEngine interface, and generally is, but there are advantages to doing
	 * it ourselves including the ability to bypass the parameter bug in releases up
	 * to 1.5.0 and managing context switches that might happen in a very
	 * theoretical world. We saw some behavior in 1.4.0 and earlier releases where
	 * this execution pattern caused a lot more interactions with the FHIR server
	 * so, care needs to be taken in its use. There are tests that check for the
	 * number of server calls.
	 * 
	 * @param libraryName    Library identifier (required)
	 * @param libraryVersion Library version (optional/null)
	 * @param parameters     parameter values for required input parameters in the
	 *                       CQL (optional/null)
	 * @param expressions    list of defines to be executed from the specified
	 *                       <code>libraryName</code> (optional/null). When not
	 *                       provided, all defines in the library will be executed.
	 * @param contextIds     list of contexts (generally patient IDs) for which the
	 *                       specified <code>expressions</code> will be executed. At
	 *                       least one value is required.
	 * @param callback       callback function to be evaluated once per context per
	 *                       executed define (required).
	 */
	protected void evaluateExpressionByExpression(final String libraryName, final String libraryVersion,
			final Map<String, Object> parameters, final Set<String> expressions, final List<String> contextIds,
			final EvaluationResultCallback callback) {
		if (this.libraries == null || this.dataServerClient == null || this.terminologyServerClient == null
				|| this.measureServerClient == null) {
			throw new IllegalArgumentException(
					"Missing one or more required initialization parameters (libraries, dataServerClient, terminologyServerClient, measureServerClient)");
		}

		if (libraryName == null || contextIds == null || contextIds.isEmpty()) {
			throw new IllegalArgumentException(
					"Missing one or more required input parameters (libraryName, contextIds)");
		}

		LibraryLoader ll = new InMemoryLibraryLoader(libraries);

		ModelResolver modelResolver = new R4FhirModelResolver();
		SearchParameterResolver resolver = new SearchParameterResolver(this.dataServerClient.getFhirContext());
		RetrieveProvider retrieveProvider = new RestFhirRetrieveProvider(resolver, this.dataServerClient);
		CompositeDataProvider dataProvider = new CompositeDataProvider(modelResolver, retrieveProvider);

		Map<String, DataProvider> dataProviders = getDataProviders(dataProvider);
		
		TerminologyProvider termProvider = new R4FhirTerminologyProvider(this.terminologyServerClient);

		VersionedIdentifier libraryId = new VersionedIdentifier().withId(libraryName);
		if (libraryVersion != null) {
			libraryId.setVersion(libraryVersion);
		}
		Library library = ll.load(libraryId);
		// TODO - check to make sure there aren't any errors in the library (see
		// library.getAnnotations())

		requireNonDefaultParameters(library, parameters);

		for (String contextId : contextIds) {
			Context context = new Context(library);
			for( Map.Entry<String,DataProvider> e : dataProviders.entrySet() ) {
				context.registerDataProvider(e.getKey(), e.getValue());
			}
			context.registerTerminologyProvider(termProvider);
			context.registerLibraryLoader(ll);
			context.setExpressionCaching(true);

			if (parameters != null) {
				for (Map.Entry<String, Object> entry : parameters.entrySet()) {
					context.setParameter(/* libraryName= */null, entry.getKey(), entry.getValue());
				}
			}

			Set<String> exprToEvaluate = new LinkedHashSet<String>();
			if (expressions != null) {
				exprToEvaluate.addAll(expressions);
			} else {
				if (library.getStatements() != null && library.getStatements().getDef() != null) {
					exprToEvaluate
							.addAll(library.getStatements().getDef().stream().filter(e -> !(e instanceof FunctionDef))
									.map(e -> e.getName()).collect(Collectors.toList()));
				}
			}

			// This style of invocation allows us to potentially be Context agnostic and
			// support switching contexts between defines if that ever becomes something we
			// need.
			// Of course, we would need to map the contextIds in the input to multiple
			// context
			// paths, so this isn't really complete yet.
			for (String expression : exprToEvaluate) {
				ExpressionDef def = context.resolveExpressionRef(expression);
				context.enterContext(def.getContext());
				context.setContextValue(context.getCurrentContext(), contextId);

				// Executor.java uses def.getExpression().evaluate(), but that causes
				// an extra evaluation for some reason.
				Object result = def.evaluate(context);

				callback.onEvaluationComplete(contextId, def.getName(), result);
			}
		}
	}

	/**
	 * Usage pattern that uses the CqlEngine interface to execute provided CQL. This
	 * is the preferred usage pattern, but
	 * {@see #evaluateExpressionByExpression(String, String, Map, Set, List,
	 * EvaluationResultCallback)} for details on why it might not be used.
	 * 
	 * @param libraryName    Library identifier
	 * @param libraryVersion Library version (optional/null)
	 * @param parameters     parameter values for required input parameters in the
	 *                       CQL (optional/null)
	 * @param expressions    list of defines to be executed from the specified
	 *                       <code>libraryName</code> (optional/null). When not
	 *                       provided, all defines in the library will be executed.
	 * @param contextIds     list of contexts (generally patient IDs) for which the
	 *                       specified <code>expressions</code> will be executed. At
	 *                       least one value is required.
	 * @param callback       callback function to be evaluated once per context per
	 *                       executed define
	 */
	protected void evaluateWithEngineWrapper(String libraryName, String libraryVersion, Map<String, Object> parameters,
			Set<String> expressions, List<String> contextIds, EvaluationResultCallback callback) {
		if (this.libraries == null || this.dataServerClient == null || this.terminologyServerClient == null
				|| this.measureServerClient == null) {
			throw new IllegalArgumentException(
					"Missing one or more required initialization parameters (libraries, dataServerClient, terminologyServerClient, measureServerClient)");
		}

		if (libraryName == null || contextIds == null || contextIds.isEmpty()) {
			throw new IllegalArgumentException(
					"Missing one or more required input parameters (libraryName, contextIds)");
		}

		LibraryLoader ll = new InMemoryLibraryLoader(libraries);

		ModelResolver modelResolver = new R4FhirModelResolver();
		SearchParameterResolver resolver = new SearchParameterResolver(this.dataServerClient.getFhirContext());
		RetrieveProvider retrieveProvider = new RestFhirRetrieveProvider(resolver, this.dataServerClient);
		CompositeDataProvider dataProvider = new CompositeDataProvider(modelResolver, retrieveProvider);

		Map<String, DataProvider> dataProviders = getDataProviders(dataProvider);

		TerminologyProvider termProvider = new R4FhirTerminologyProvider(this.terminologyServerClient);

		VersionedIdentifier libraryId = new VersionedIdentifier().withId(libraryName);
		if (libraryVersion != null) {
			libraryId.setVersion(libraryVersion);
		}

		Library library = ll.load(libraryId);
		requireNonDefaultParameters(library, parameters);

		CqlEngine cqlEngine = new CqlEngine(ll, dataProviders, termProvider);

		for (String contextId : contextIds) {
			EvaluationResult er = cqlEngine.evaluate(libraryId, expressions, Pair.of(ContextNames.PATIENT, contextId),
					parameters, /* debugMap= */null);
			for (Map.Entry<String, Object> result : er.expressionResults.entrySet()) {
				callback.onEvaluationComplete(contextId, result.getKey(), result.getValue());
			}
		}
	}

	/**
	 * Helper method for turning the list of supported models into a Map of DataProviders
	 * to be used when registering with the CQLEngine.
	 * @param dataProvider DataProvider that will be used in support of the SUPPORTED_MODELS
	 * @return Map of model url to the <code>dataProvider</code>
	 */
	protected Map<String, DataProvider> getDataProviders(CompositeDataProvider dataProvider) {
		Map<String, DataProvider> dataProviders = SUPPORTED_MODELS.stream().map(url -> Map.entry(url, dataProvider))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return dataProviders;
	}

	/**
	 * Execute the given <code>libraryName</code> for each context id specified in
	 * <code>contextIds</code> using a FHIR R4 data provider and FHIR R4 terminology
	 * provider. Library content and FHIR server configuration data should be
	 * configured prior to invoking this method.
	 * 
	 * @param libraryName    Library identifier
	 * @param libraryVersion Library version (optional/null)
	 * @param parameters     parameter values for required input parameters in the
	 *                       CQL (optional/null)
	 * @param expressions    list of defines to be executed from the specified
	 *                       <code>libraryName</code> (optional/null). When not
	 *                       provided, all defines in the library will be executed.
	 * @param contextIds     list of contexts (generally patient IDs) for which the
	 *                       specified <code>expressions</code> will be executed. At
	 *                       least one value is required.
	 * @param callback       callback function to be evaluated once per context per
	 *                       executed define
	 */
	public void evaluate(String libraryName, String libraryVersion, Map<String, Object> parameters,
			Set<String> expressions, List<String> contextIds, EvaluationResultCallback callback) {
		evaluateWithEngineWrapper(libraryName, libraryVersion, parameters, expressions, contextIds, callback);
	}

	/**
	 * Interrogate the CQL Library for parameters with no default values and throw
	 * and exception when the parameters collection is missing at least one of those
	 * values.
	 * 
	 * @param library    CQL Library
	 * @param parameters parameter values, expected to include all non-default
	 *                   parameters in the provided library
	 */
	private void requireNonDefaultParameters(Library library, Map<String, Object> parameters) {
		List<String> missingParameters = new ArrayList<>();

		if (library.getParameters() != null) {
			for (ParameterDef pd : library.getParameters().getDef()) {
				if (pd.getDefault() == null) {
					if (parameters == null || !parameters.containsKey(pd.getName())) {
						missingParameters.add(pd.getName());
					}
				}
			}
		}

		if (!missingParameters.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("Missing parameter values for one or more non-default library parameters {}",
							missingParameters.toString()));
		}
	}

	/**
	 * Command line argument definitions
	 */
	private static final class Arguments {
		@Parameter(names = { "-f",
				"--files" }, description = "Resource that contains the CQL library sources. Valid options are the path to a zip file or folder containing the cohort definitions or the resource ID of a FHIR Library resource contained in the measure server.", required = true)
		private String libraryPath;

		@Parameter(names = { "-d",
				"--data-server" }, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve data.", required = true)
		private File dataServerConfigFile;

		@Parameter(names = { "-t",
				"--terminology-server" }, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve terminology.", required = false)
		private File terminologyServerConfigFile;

		@Parameter(names = { "-m",
				"--measure-server" }, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve measure and library resources.", required = false)
		private File measureServerConfigFile;

		@Parameter(names = { "-l",
				"--libraryName" }, description = "Library Name (from CQL Library statement)", required = true)
		private String libraryName;

		@Parameter(names = { "-v",
				"--libraryVersion" }, description = "Library Version (from CQL Library statement)", required = false)
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
		
		@Parameter(names = { "-h",
				"--help" }, description = "Display this help", required = false, help = true)
		private boolean isDisplayHelp;
	}

	/**
	 * Conversion routine for CQL parameter values encoded for command line
	 * interaction.
	 * 
	 * @param arguments list of CQL parameter values encoded as strings
	 * @return decoded parameter values formatted for consumption by the CQL engine
	 */
	protected static Map<String, Object> parseParameters(List<String> arguments) {
		Map<String, Object> result = new HashMap<>();

		Pattern p = Pattern.compile("(?<name>[^:]+):(?<type>[^:]+):(?<value>.*)");
		for (String arg : arguments) {
			Matcher m = p.matcher(arg);
			if (m.matches()) {
				String name = m.group("name");
				String type = m.group("type");
				String value = m.group("value");

				Object typedValue = null;
				String[] parts = null;
				switch (type) {
				case "integer":
					typedValue = Integer.parseInt(value);
					break;
				case "decimal":
					typedValue = new BigDecimal(value);
					break;
				case "boolean":
					typedValue = Boolean.parseBoolean(value);
					break;
				case "string":
					typedValue = value;
					break;
				case "datetime":
					typedValue = resolveDateTimeParameter(value);
					break;
				case "time":
					typedValue = new Time(value);
					break;
				case "quantity":
					typedValue = resolveQuantityParameter(value);
					break;
				case "code":
					typedValue = resolveCodeParameter(value);
					break;
				case "concept":
					throw new UnsupportedOperationException("No support for concept type parameters");
				case "interval":
					parts = value.split(",");
					String subType = parts[0];
					String start = parts[1];
					String end = parts[2];

					switch (subType) {
					case "integer":
						typedValue = new Interval(Integer.parseInt(start), true, Integer.parseInt(end), true);
						break;
					case "decimal":
						typedValue = new Interval(new BigDecimal(start), true, new BigDecimal(end), true);
						break;
					case "quantity":
						typedValue = new Interval(resolveQuantityParameter(start), true, resolveQuantityParameter(end),
								true);
						break;
					case "datetime":
						typedValue = new Interval(resolveDateTimeParameter(start), true, resolveDateTimeParameter(end),
								true);
						break;
					case "time":
						typedValue = new Interval(new Time(start), true, new Time(end), true);
						break;
					default:
						throw new IllegalArgumentException(String.format("Unsupported interval type %s", subType));
					}
					break;
				default:
					throw new IllegalArgumentException(String.format("Parameter type %s not supported", type));
				}

				result.put(name, typedValue);
			} else {
				throw new IllegalArgumentException(String.format("Invalid parameter string %s", arg));
			}
		}

		return result;
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	protected static Object resolveCodeParameter(String value) {
		Object typedValue;
		String[] parts;
		parts = value.trim().split(":");
		typedValue = new Code().withCode(parts[0]).withSystem(parts[1]).withDisplay(parts[2]);
		return typedValue;
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	protected static Object resolveDateTimeParameter(String value) {
		Object typedValue;
		typedValue = new DateTime(value.replace("@", ""), OffsetDateTime.now().getOffset());
		return typedValue;
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	protected static Object resolveQuantityParameter(String value) {
		Object typedValue;
		String[] parts;
		parts = value.trim().split(":");
		typedValue = new Quantity().withValue(new BigDecimal(parts[0])).withUnit(parts[1]);
		return typedValue;
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
	public void runWithArgs(String[] args, PrintStream out) throws Exception {
		Arguments arguments = new Arguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("cql-engine").console(console).addObject(arguments).build();
		jc.parse(args);
		
		if( arguments.isDisplayHelp ) {
			jc.usage();
		} else {

			ObjectMapper om = new ObjectMapper();
	
			FhirServerConfig dataConfig = om.readValue(arguments.dataServerConfigFile, FhirServerConfig.class);
			setDataServerConnectionProperties(dataConfig);
	
			FhirServerConfig terminologyConfig = dataConfig;
			if (arguments.terminologyServerConfigFile != null) {
				terminologyConfig = om.readValue(arguments.terminologyServerConfigFile, FhirServerConfig.class);
			}
			setTerminologyServerConnectionProperties(terminologyConfig);
	
			FhirServerConfig measureConfig = dataConfig;
			if (arguments.measureServerConfigFile != null) {
				measureConfig = om.readValue(arguments.measureServerConfigFile, FhirServerConfig.class);
			}
			setMeasureServerConnectionProperties(measureConfig);
	
			Path libraryFolder = Paths.get(arguments.libraryPath);
			out.println(String.format("Loading libraries from %s ...", libraryFolder.toString()));
			if (libraryFolder.toFile().isDirectory()) {
				addLibrariesFromFolder(libraryFolder, arguments.sourceFormat);
			} else if (libraryFolder.toFile().isFile() && libraryFolder.toString().endsWith(".zip")) {
				addLibrariesFromZip(libraryFolder, arguments.sourceFormat);
			} else {
				// if all else fails, assume that the argument value is a FHIR library resource
				// ID
				addLibrariesFromFhirResource(arguments.libraryPath, arguments.sourceFormat);
			}
	
			Map<String, Object> parameters = null;
			if (arguments.parameters != null) {
				parameters = parseParameters(arguments.parameters);
			}
	
			evaluate(arguments.libraryName, arguments.libraryVersion, parameters, arguments.expressions,
					arguments.contextIds, (contextId, expression, result) -> {
						out.println(String.format("Expression: %s, Context: %s, Result: %s", expression, contextId,
								(result != null) ? String.format("%s", result.toString()) : "null"));
					});
		}
	}

	public static void main(String[] args) throws Exception {
		CqlEngineWrapper wrapper = new CqlEngineWrapper();
		wrapper.runWithArgs(args, System.out);
	}
}
