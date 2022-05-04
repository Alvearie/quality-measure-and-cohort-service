/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure.seed;

import static com.ibm.cohort.cql.cdm.CDMConstants.MEASURE_PARAMETER_URL;
import static com.ibm.cohort.cql.cdm.CDMConstants.PARAMETER_DEFAULT_URL;

import java.util.List;
import java.util.Map;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.measure.LibraryDependencyGatherer;
import com.ibm.cohort.measure.ParameterConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ParameterDefinition;
import org.opencds.cqf.common.helpers.UsingHelper;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.debug.DebugMap;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.cohort.measure.cqfruler.CDMContext;
import com.ibm.cohort.measure.helpers.MeasurementPeriodHelper;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;

/*
 * KWAS BIG TODO: Handle extensions somehow???
 *           Need a parameter definition handler or something...it can't fit the existing resource field handler.
 *           Rename resourcefieldhandler to knowledgeresourcefieldhandler maybe???
 */
public class MeasureEvaluationSeeder<L, M, PD, I> {

	private final TerminologyProvider terminologyProvider;
	private final Map<String, CqlDataProvider> dataProviders;
	private final LibraryDependencyGatherer<L, M> libraryDependencyGatherer;
	private final FhirResourceResolver<L> libraryResolver;
	private final ParameterConverter<PD> parameterConverter;
	private final LibraryLoader libraryLoader;
	private final ResourceFieldHandler<L, I> libraryHandler;
	private final ResourceFieldHandler<M, I> measureHandler;

	private boolean enableExpressionCaching;
	private boolean debugMode = true;
	
	private static final String MEASUREMENT_PERIOD = "Measurement Period";

	public MeasureEvaluationSeeder(
			TerminologyProvider terminologyProvider,
			Map<String, CqlDataProvider> dataProviders,
			LibraryDependencyGatherer<L, M> libraryDependencyGatherer,
			FhirResourceResolver<L> libraryResolver,
			ParameterConverter<PD> parameterConverter,
			LibraryLoader libraryLoader,
			ResourceFieldHandler<L, I> libraryHandler,
			ResourceFieldHandler<M, I> measureHandler) {
		this.terminologyProvider = terminologyProvider;
		this.dataProviders = dataProviders;
		this.libraryDependencyGatherer = libraryDependencyGatherer;
		this.libraryResolver = libraryResolver;
		this.parameterConverter = parameterConverter;
		this.libraryLoader = libraryLoader;
		this.libraryHandler = libraryHandler;
		this.measureHandler = measureHandler;
	}

	public MeasureEvaluationSeeder disableDebugLogging() {
		this.debugMode = false;

		return this;
	}

	public MeasureEvaluationSeeder enableExpressionCaching() {
		this.enableExpressionCaching = true;

		return this;
	}

	public IMeasureEvaluationSeed<M> create(M measure, String periodStart, String periodEnd, String productLine, Map<String, Parameter> parameters) {
		// Gather the primary library and all of its dependencies
		List<L> fhirLibraries = libraryDependencyGatherer.gatherForMeasure(measure);

		if( CollectionUtils.isEmpty(fhirLibraries) ) {
			throw new IllegalArgumentException(String.format("No libraries were able to be loaded for %s", measureHandler.getId(measure)));
		}
		
		// the "primary" library is always the first library loaded for the measure
		L primaryFhirLibrary = fhirLibraries.get(0);
		VersionedIdentifier libraryIdentifier = new VersionedIdentifier()
				.withId(libraryHandler.getName(primaryFhirLibrary))
				.withVersion(libraryHandler.getVersion(primaryFhirLibrary));
		org.cqframework.cql.elm.execution.Library primaryLibrary = libraryLoader.load(libraryIdentifier);
		
		List<Triple<String, String, String>> usingDefs = UsingHelper.getUsingUrlAndVersion(primaryLibrary.getUsings());

		if (usingDefs.size() > 1) {
			throw new IllegalArgumentException("Evaluation of Measure using multiple Models is not supported at this time.");
		}

		// Per the above condition, we should only have one model per measure
		String lastModelUri = usingDefs.get(usingDefs.size() - 1).getRight();
		DataProvider dataProvider = dataProviders.get(lastModelUri);

		Context context = createContext(primaryLibrary, lastModelUri, dataProvider, productLine, libraryLoader);

		/*
		 * KWAS TODO: Somehow handle CDM extensions in a sane way
		 */
		// fhir path: Measure.extension[measureParameter][].valueParameterDefinition.extension[defaultValue]
		measure.getExtension().stream()
				.filter(MeasureEvaluationSeeder::isMeasureParameter)
				.map(parameter -> dataProvider.resolvePath(parameter, "valueParameterDefinition"))
				.map(ParameterDefinition.class::cast)
				.forEach(parameterDefinition -> setDefaultValue(context, parameterDefinition));

		if (parameters != null) {
			parameters.entrySet().stream()
					.forEach(e -> context.setParameter(null, e.getKey(), e.getValue().toCqlType()));

		}

		// Set measurement period last to make sure we respect periodStart
		// and periodEnd date boundaries for an execution.
		Interval measurementPeriod = createMeasurePeriod(periodStart, periodEnd);
		context.setParameter(null, MEASUREMENT_PERIOD, measurementPeriod);

		return new CustomMeasureEvaluationSeed<>(measure, context, measurementPeriod, dataProvider);
	}

	protected Context createContext(
			org.cqframework.cql.elm.execution.Library library,
			String modelUri,
			DataProvider dataProvider,
			String productLine,
			LibraryLoader libraryLoader) {

		Context context = createDefaultContext(library);
		context.registerLibraryLoader(libraryLoader);
		context.registerTerminologyProvider(terminologyProvider);
		context.registerDataProvider(modelUri, dataProvider);

		if (productLine != null) {
			context.setParameter(null, "Product Line", productLine);
		}

		if (enableExpressionCaching) {
			context.setExpressionCaching(true);
		}

		if (debugMode) {
			DebugMap debugMap = new DebugMap();
			debugMap.setIsLoggingEnabled(true);
			context.setDebugMap(debugMap);
		}

		return context;
	}

	protected Context createDefaultContext(org.cqframework.cql.elm.execution.Library library) {
		return new CDMContext(library);
	}

	protected Interval createMeasurePeriod(String periodStart, String periodEnd) {
		return new Interval(MeasurementPeriodHelper.getPeriodStart(periodStart), true,
							MeasurementPeriodHelper.getPeriodEnd(periodEnd), true);
	}

	private void setDefaultValue(Context context, PD parameterDefinition) {
		Parameter parameter = parameterConverter.toCohortParameter(parameterDefinition);
		if (parameter != null) {
			context.setParameter(null, parameterDefinition.getName(), parameter.toCqlType());
		}
	}

	public static boolean isMeasureParameter(Extension extension) {
		return MEASURE_PARAMETER_URL.equals(extension.getUrl());
	}

	public static boolean isDefaultValue(Extension extension) {
		return PARAMETER_DEFAULT_URL.equals(extension.getUrl());
	}
}
