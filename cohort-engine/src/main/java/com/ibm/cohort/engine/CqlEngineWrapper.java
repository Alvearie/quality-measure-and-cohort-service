/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.elm.execution.ExpressionDef;
import org.cqframework.cql.elm.execution.FunctionDef;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.retrieve.RestFhirRetrieveProvider;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.fhir.terminology.R4FhirTerminologyProvider;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.cohort.engine.cdm.CDMConstants;
import com.ibm.cohort.engine.cqfruler.CDMContext;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Provide a wrapper around the open source reference implementation of a CQL
 * engine using sensible defaults for our target execution environment and
 * providing utilities for loading library contents from various sources.
 */
public class CqlEngineWrapper {

	public static final List<String> SUPPORTED_MODELS = Arrays.asList("http://hl7.org/fhir",
			"http://hl7.org/fhir/us/core", "http://hl7.org/fhir/us/qicore", CDMConstants.BASE_URL);

	/*
	 * Wrap the ModelResolver around a static ThreadLocal to prevent
	 * excess creation of FhirContext instances.
	 */
	private static final ThreadLocal<ModelResolver> MODEL_RESOLVER = ThreadLocal.withInitial(R4FhirModelResolver::new);

	private LibraryLoader libraryLoader = null;

	private FhirClientBuilder clientBuilder;

	private IGenericClient dataServerClient;
	private IGenericClient measureServerClient;
	private IGenericClient terminologyServerClient;

	public CqlEngineWrapper() {
		this(FhirClientBuilderFactory.newInstance().newFhirClientBuilder(FhirContext.forR4()));
	}

	public CqlEngineWrapper(FhirClientBuilder clientBuilder) {
		this.clientBuilder = clientBuilder;
	}

	/**
	 * Configure the library loader to be used when loading CQL libraries.
	 * 
	 * @param libraryLoader the libraryLoader
	 */
	public void setLibraryLoader(LibraryLoader libraryLoader) {
		this.libraryLoader = libraryLoader;
	}

	/**
	 * Configure the FHIR server used for retrieve operations.
	 * 
	 * @param config data server connection properties
	 */
	public void setDataServerConnectionProperties(FhirServerConfig config) {
		setDataServerClient(clientBuilder.createFhirClient(config));
	}

	/**
	 * Set the FHIR client used for data operations.
	 * 
	 * @param client data server client
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
	 * @param config measure server connection properties
	 */
	public void setMeasureServerConnectionProperties(FhirServerConfig config) {
		setMeasureServerClient(clientBuilder.createFhirClient(config));
	}

	/**
	 * Set the FHIR client used to interact with the FHIR measure server.
	 * 
	 * @param client measure server client
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
	 * @param config terminology server connection properties
	 */
	public void setTerminologyServerConnectionProperties(FhirServerConfig config) {
		this.terminologyServerClient = clientBuilder.createFhirClient(config);
	}

	/**
	 * Set the FHIR client used for terminology data access.
	 * 
	 * @param client terminology server client
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
		if (this.libraryLoader == null || this.dataServerClient == null || this.terminologyServerClient == null
				|| this.measureServerClient == null) {
			throw new IllegalArgumentException(
					"Missing one or more required initialization parameters (libraries, dataServerClient, terminologyServerClient, measureServerClient)");
		}

		if (libraryName == null || contextIds == null || contextIds.isEmpty()) {
			throw new IllegalArgumentException(
					"Missing one or more required input parameters (libraryName, contextIds)");
		}

		Map<String, DataProvider> dataProviders = getDataProviders();

		TerminologyProvider termProvider = getTerminologyProvider();

		VersionedIdentifier libraryId = new VersionedIdentifier().withId(libraryName);
		if (libraryVersion != null) {
			libraryId.setVersion(libraryVersion);
		}

		Library library = libraryLoader.load(libraryId);
		LibraryUtils.requireNoTranslationErrors(library);
		LibraryUtils.requireValuesForNonDefaultParameters(library, parameters);

		for (String contextId : contextIds) {
			callback.onContextBegin(contextId);

			Context context = new CDMContext(library);
			for (Map.Entry<String, DataProvider> e : dataProviders.entrySet()) {
				context.registerDataProvider(e.getKey(), e.getValue());
			}
			context.registerTerminologyProvider(termProvider);
			context.registerLibraryLoader(libraryLoader);
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

			callback.onContextComplete(contextId);
		}
	}

	/**
	 * Initialize the data providers for the CQL Engine
	 * 
	 * @return Map of supported model URL to data provider
	 */
	protected Map<String, DataProvider> getDataProviders() {
		SearchParameterResolver resolver = new SearchParameterResolver(this.dataServerClient.getFhirContext());
		RetrieveProvider retrieveProvider = new RestFhirRetrieveProvider(resolver, this.dataServerClient);
		CompositeDataProvider dataProvider = new CompositeDataProvider(MODEL_RESOLVER.get(), retrieveProvider);

		Map<String, DataProvider> dataProviders = mapSupportedModelsToDataProvider(dataProvider);
		return dataProviders;
	}

	/**
	 * Helper method for turning the list of supported models into a Map of
	 * DataProviders to be used when registering with the CQLEngine.
	 * 
	 * @param dataProvider DataProvider that will be used in support of the
	 *                     SUPPORTED_MODELS
	 * @return Map of model url to the <code>dataProvider</code>
	 */
	protected Map<String, DataProvider> mapSupportedModelsToDataProvider(DataProvider dataProvider) {
		return mapSupportedModelsToDataProvider(SUPPORTED_MODELS, dataProvider);
	}

	/**
	 * Helper method for turning the list of supported models into a Map of
	 * DataProviders to be used when registering with the CQLEngine.
	 * 
	 * @param supportedModels List of data models that are supported (i.e. base FHIR, QICore, etc)
	 * @param dataProvider DataProvider that will be used in support of the
	 *                     SUPPORTED_MODELS
	 * @return Map of model url to the <code>dataProvider</code>
	 */
	protected Map<String, DataProvider> mapSupportedModelsToDataProvider(List<String> supportedModels,
			DataProvider dataProvider) {
		return supportedModels.stream()
				.map(url -> new SimpleEntry<String, DataProvider>(url, dataProvider))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Initialize the terminology provider for the CQL Engine
	 * 
	 * @return terminology provider
	 */
	protected TerminologyProvider getTerminologyProvider() {
		return new R4FhirTerminologyProvider(this.terminologyServerClient);
	}

	/**
	 * Usage pattern that uses the CqlEngine interface to execute provided CQL. This
	 * is the preferred usage pattern, but
	 * @see #evaluateExpressionByExpression(String, String, Map, Set, List,
	 * EvaluationResultCallback) for details on why it might not be used.
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
		if (this.libraryLoader == null || this.dataServerClient == null || this.terminologyServerClient == null
				|| this.measureServerClient == null) {
			throw new IllegalArgumentException(
					"Missing one or more required initialization parameters (libraries, dataServerClient, terminologyServerClient, measureServerClient)");
		}

		if (libraryName == null || contextIds == null || contextIds.isEmpty()) {
			throw new IllegalArgumentException(
					"Missing one or more required input parameters (libraryName, contextIds)");
		}

		Map<String, DataProvider> dataProviders = getDataProviders();

		TerminologyProvider termProvider = getTerminologyProvider();

		VersionedIdentifier libraryId = new VersionedIdentifier().withId(libraryName);
		if (libraryVersion != null) {
			libraryId.setVersion(libraryVersion);
		}

		Library library = libraryLoader.load(libraryId);
		LibraryUtils.requireNoTranslationErrors(library);
		LibraryUtils.requireValuesForNonDefaultParameters(library, parameters);

		CqlEngine cqlEngine = new CqlEngine(libraryLoader, dataProviders, termProvider);

		for (String contextId : contextIds) {
			callback.onContextBegin(contextId);
			EvaluationResult er = cqlEngine.evaluate(libraryId, expressions, Pair.of(ContextNames.PATIENT, contextId),
					parameters, /* debugMap= */null);
			for (Map.Entry<String, Object> result : er.expressionResults.entrySet()) {
				callback.onEvaluationComplete(contextId, result.getKey(), result.getValue());
			}
			callback.onContextComplete(contextId);
		}
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
	 * @param callback       callback function for receiving engine execution events
	 */
	public void evaluate(String libraryName, String libraryVersion, Map<String, Object> parameters,
			Set<String> expressions, List<String> contextIds, EvaluationResultCallback callback) {
		evaluateWithEngineWrapper(libraryName, libraryVersion, parameters, expressions, contextIds, callback);
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
			Set<String> expressions, List<String> contextIds, ExpressionResultCallback callback) {
		evaluateWithEngineWrapper(libraryName, libraryVersion, parameters, expressions, contextIds,
				new ProxyingEvaluationResultCallback(callback));
	}
}
