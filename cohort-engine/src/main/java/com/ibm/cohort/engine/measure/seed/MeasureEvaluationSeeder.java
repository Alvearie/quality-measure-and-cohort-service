package com.ibm.cohort.engine.measure.seed;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.common.evaluation.EvaluationProviderFactory;
import org.opencds.cqf.common.helpers.DateHelper;
import org.opencds.cqf.common.helpers.UsingHelper;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.debug.DebugMap;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.google.common.annotations.VisibleForTesting;
import com.ibm.cohort.engine.cqfruler.DefineContext;
import com.ibm.cohort.engine.measure.LibraryHelper;

public class MeasureEvaluationSeeder {

	private final EvaluationProviderFactory providerFactory;
	private final LibraryLoader libraryLoader;
	private final LibraryResolutionProvider<Library> libraryResourceProvider;

	private boolean enableExpressionCaching;
	private boolean debugMode = true;
	private String terminologyProviderSource;
	private String terminologyProviderUser;
	private String terminologyProviderPassword;

	public MeasureEvaluationSeeder(
			EvaluationProviderFactory providerFactory,
			LibraryLoader libraryLoader,
			LibraryResolutionProvider<Library> libraryResourceProvider) {
		this.providerFactory = providerFactory;
		this.libraryLoader = libraryLoader;
		this.libraryResourceProvider = libraryResourceProvider;
	}

	public MeasureEvaluationSeeder disableDebugLogging() {
		this.debugMode = false;

		return this;
	}

	public MeasureEvaluationSeeder enableExpressionCaching() {
		this.enableExpressionCaching = true;

		return this;
	}


	public MeasureEvaluationSeeder withTerminologyProvider(String source, String user, String password) {
		this.terminologyProviderSource = source;
		this.terminologyProviderUser = user;
		this.terminologyProviderPassword = password;

		return this;
	}


	public IMeasureEvaluationSeed create(Measure measure, String periodStart, String periodEnd, String productLine) {
		List<org.cqframework.cql.elm.execution.Library> libraries = LibraryHelper.loadLibraries(measure, this.libraryLoader, this.libraryResourceProvider);
		if( CollectionUtils.isEmpty(libraries) ) { 
			throw new IllegalArgumentException(String.format("No libraries were able to be loaded for Measure/%s", measure.getId()));
		}
		
		// the "primary" library is always the first library loaded for the measure
		org.cqframework.cql.elm.execution.Library primaryLibrary = libraries.get(0);
		
		List<Triple<String, String, String>> usingDefs = UsingHelper.getUsingUrlAndVersion(primaryLibrary.getUsings());

		if (usingDefs.size() > 1) {
			throw new IllegalArgumentException("Evaluation of Measure using multiple Models is not supported at this time.");
		}


		TerminologyProvider terminologyProvider = createTerminologyProvider(usingDefs);
		Map<Triple<String, String, String>, DataProvider> dataProviders = createDataProviders(usingDefs, terminologyProvider);
		Interval measurementPeriod = createMeasurePeriod(periodStart, periodEnd);
		Context context = createContext(primaryLibrary, dataProviders, terminologyProvider, measurementPeriod, productLine);

		List<Map.Entry<Triple<String, String, String>, DataProvider>> dataProviderList = new ArrayList<>(dataProviders.entrySet());
		DataProvider lastDataProvider = dataProviderList.get(dataProviderList.size() - 1).getValue();

		return new CustomMeasureEvaluationSeed(measure, context, measurementPeriod, lastDataProvider);
	}

	@VisibleForTesting
	protected Context createContext(
			org.cqframework.cql.elm.execution.Library library,
			Map<Triple<String, String, String>, DataProvider> dataProviders,
			TerminologyProvider terminologyProvider,
			Interval measurementPeriod,
			String productLine) {

		Context context = createDefaultContext(library);
		context.registerLibraryLoader(libraryLoader);

		if (!dataProviders.isEmpty()) {
			context.registerTerminologyProvider(terminologyProvider);
		}

		for (Map.Entry<Triple<String, String, String>, DataProvider> dataProviderEntry : dataProviders.entrySet()) {
			Triple<String, String, String> usingDef = dataProviderEntry.getKey();
			DataProvider dataProvider = dataProviderEntry.getValue();

			context.registerDataProvider(usingDef.getRight(), dataProvider);
		}

		context.setParameter(
		null,
                "Measurement Period",
                new Interval(DateTime.fromJavaDate((Date) measurementPeriod.getStart()), true,
                             DateTime.fromJavaDate((Date) measurementPeriod.getEnd()), true));

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

	@VisibleForTesting
	protected Context createDefaultContext(org.cqframework.cql.elm.execution.Library library) {
		return new DefineContext(library);
	}

	@VisibleForTesting
	protected Interval createMeasurePeriod(String periodStart, String periodEnd) {
		return new Interval(DateHelper.resolveRequestDate(periodStart, true), true,
							DateHelper.resolveRequestDate(periodEnd, false), true);
	}


	private Map<Triple<String, String, String>, DataProvider> createDataProviders(
			Iterable<Triple<String, String, String>> usingDefs,
			TerminologyProvider terminologyProvider) {
		LinkedHashMap<Triple<String, String, String>, DataProvider> dataProviders = new LinkedHashMap<>();

		for (Triple<String, String, String> def : usingDefs) {
			dataProviders.put(def, this.providerFactory.createDataProvider(def.getLeft(), def.getMiddle(), terminologyProvider));
		}

		return dataProviders;
	}

	private TerminologyProvider createTerminologyProvider(List<Triple<String, String, String>> usingDefs) {
		// If there are no Usings, there is probably not any place the Terminology
		// actually used so I think the assumption that at least one provider exists is
		// ok.
		TerminologyProvider terminologyProvider = null;
		if (!usingDefs.isEmpty()) {
			// Creates a terminology provider based on the first using statement. This
			// assumes the terminology
			// server matches the FHIR version of the CQL.
			terminologyProvider = this.providerFactory.createTerminologyProvider(
				usingDefs.get(0).getLeft(), 
				usingDefs.get(0).getMiddle(), 
				terminologyProviderSource, 
				terminologyProviderUser, 
				terminologyProviderPassword);
		}

		return terminologyProvider;
	}

}
