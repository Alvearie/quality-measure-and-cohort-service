package com.ibm.cohort.engine.measure.seed;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
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
import com.ibm.cohort.engine.measure.LibraryHelper;

public class MeasureEvaluationSeeder {

	private final TerminologyProvider terminologyProvider;
	private final Map<String, DataProvider> dataProviders;
	private final LibraryLoader libraryLoader;
	private final LibraryResolutionProvider<Library> libraryResourceProvider;

	private boolean enableExpressionCaching;
	private boolean debugMode = true;

	public MeasureEvaluationSeeder(
			TerminologyProvider terminologyProvider,
			Map<String, DataProvider> dataProviders,
			LibraryLoader libraryLoader,
			LibraryResolutionProvider<Library> libraryResourceProvider) {
		this.terminologyProvider = terminologyProvider;
		this.dataProviders = dataProviders;
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

		// Per the above condition, we should only have one model per measure
		String lastModelUri = usingDefs.get(usingDefs.size() - 1).getRight();
		DataProvider dataProvider = dataProviders.get(lastModelUri);

		Interval measurementPeriod = createMeasurePeriod(periodStart, periodEnd);
		Context context = createContext(primaryLibrary, lastModelUri, dataProvider, measurementPeriod, productLine);

		return new CustomMeasureEvaluationSeed(measure, context, measurementPeriod, dataProvider);
	}

	@VisibleForTesting
	protected Context createContext(
			org.cqframework.cql.elm.execution.Library library,
			String modelUri,
			DataProvider dataProvider,
			Interval measurementPeriod,
			String productLine) {

		Context context = createDefaultContext(library);
		context.registerLibraryLoader(libraryLoader);
		context.registerTerminologyProvider(terminologyProvider);
		context.registerDataProvider(modelUri, dataProvider);

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
		return new Context(library);
	}

	@VisibleForTesting
	protected Interval createMeasurePeriod(String periodStart, String periodEnd) {
		return new Interval(DateHelper.resolveRequestDate(periodStart, true), true,
							DateHelper.resolveRequestDate(periodEnd, false), true);
	}

}
