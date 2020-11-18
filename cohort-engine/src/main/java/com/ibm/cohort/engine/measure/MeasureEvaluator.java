/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.opencds.cqf.common.evaluation.EvaluationProviderFactory;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.debug.DebugMap;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.r4.evaluation.MeasureEvaluationSeed;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Provide an interface for doing quality measure evaluation against a FHIR R4
 * server.
 */
public class MeasureEvaluator {

	private IGenericClient dataClient;
	private IGenericClient terminologyClient;
	private IGenericClient measureClient;
	private MeasureResolutionProvider<Measure> provider = null;
	private MeasurementPeriodStrategy measurementPeriodStrategy;

	
	public MeasureEvaluator(IGenericClient dataClient, IGenericClient terminologyClient, IGenericClient measureClient) {
		this.dataClient = dataClient;
		this.terminologyClient = terminologyClient;
		this.measureClient = measureClient; 
	}
	
	public void setMeasureResolutionProvider(MeasureResolutionProvider<Measure> provider) {
		this.provider = provider;
	}
	
	public MeasureResolutionProvider<Measure> getMeasureResolutionProvider() {
		if( this.provider == null ) {
			this.provider = new RestFhirMeasureResolutionProvider(measureClient);
		}
		return this.provider;
	}

	public void setMeasurementPeriodStrategy(MeasurementPeriodStrategy strategy) {
		this.measurementPeriodStrategy = strategy;
	}

	public MeasurementPeriodStrategy getMeasurementPeriodStrategy() {
		if (this.measurementPeriodStrategy == null) {
			this.measurementPeriodStrategy = new DefaultMeasurementPeriodStrategy();
		}
		return this.measurementPeriodStrategy;
	}
	
	public List<MeasureReport> evaluatePatientMeasures(String patientId, List<MeasureContext> measureContexts) {
		List<MeasureReport> measureReports = new ArrayList<>();
		MeasureReport measureReport = null;
		boolean inInitialPopulation;
		for (MeasureContext measureContext: measureContexts) {
			inInitialPopulation = false;
			measureReport = evaluatePatientMeasure(measureContext.getMeasureId(), patientId, measureContext.getParameters());
			if (measureReport != null) {
				for (MeasureReport.MeasureReportGroupComponent group : measureReport.getGroup()) {
					if (CDMMeasureEvaluation.StandardReportResults.fromMeasureReportGroup(group).inInitialPopulation()) {
						inInitialPopulation = true;
					}
				}
			}
			if (inInitialPopulation) {
				measureReports.add(measureReport);
			}
		}
		return measureReports;
	}

	public MeasureReport evaluatePatientMeasure(String measureId, String patientId, Map<String, Object> parameters) {
		Measure measure = MeasureHelper.loadMeasure(measureId, getMeasureResolutionProvider());
		return evaluatePatientMeasure(measure, patientId, parameters);
	}

	public MeasureReport evaluatePatientMeasure(Measure measure, String patientId, Map<String, Object> parameters) {
		Pair<String, String> period = getMeasurementPeriodStrategy().getMeasurementPeriod();
		return evaluatePatientMeasure(measure, patientId, period.getLeft(), period.getRight(), parameters);
	}

	public MeasureReport evaluatePatientMeasure(Measure measure, String patientId, String periodStart, String periodEnd,
			Map<String, Object> parameters) {
		LibraryResolutionProvider<Library> libraryResolutionProvider = new RestFhirLibraryResolutionProvider(
				measureClient);
		LibraryLoader libraryLoader = LibraryHelper.createLibraryLoader(libraryResolutionProvider);

		EvaluationProviderFactory factory = new ProviderFactory(dataClient, terminologyClient);
		MeasureEvaluationSeed seed = new MeasureEvaluationSeed(factory, libraryLoader, libraryResolutionProvider);

		// TODO - consider talking with OSS project about making source, user, and pass
		// a properties collection for more versatile configuration of the underlying
		// providers. For example, we need an additional custom HTTP header for
		// configuration of our FHIR server.
		seed.setup(measure, periodStart, periodEnd, /* productLine= */"ProductLine", /* source= */"", /* user= */"",
				/* pass= */"");
		
		// This is enabled by default in cqf-ruler and we don't want it.
		DebugMap debugMap = new DebugMap();
		debugMap.setIsLoggingEnabled(false);
		seed.getContext().setDebugMap(debugMap);

		// TODO - The OSS logic converts the period start and end into an
		// Interval and creates a parameter named "Measurement Period" that is populated
		// with that value. We need to sync with the authoring and clinical informatics
		// teams to confirm that every measure will have a measurement period and to
		// agree on what the name will be for that parameter. It is relevant during
		// MeasureReport generation as the value for the period attribute.

		// The OSS implementation doesn't support any additional parameter overrides, so
		// we need to add them ourselves.
		if (parameters != null) {
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				seed.getContext().setParameter(null, entry.getKey(), entry.getValue());
			}
		}

		CDMMeasureEvaluation evaluation = new CDMMeasureEvaluation(seed.getDataProvider(), seed.getMeasurementPeriod());
		return evaluation.evaluatePatientMeasure(measure, seed.getContext(), patientId);
	}
}
