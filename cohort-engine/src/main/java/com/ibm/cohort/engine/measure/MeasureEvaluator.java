/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.opencds.cqf.common.evaluation.EvaluationProviderFactory;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.engine.measure.seed.IMeasureEvaluationSeed;
import com.ibm.cohort.engine.measure.seed.MeasureEvaluationSeeder;

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
	private LibraryResolutionProvider<Library> libraryProvider = null;
	private MeasurementPeriodStrategy measurementPeriodStrategy;

	public MeasureEvaluator(IGenericClient dataClient, IGenericClient terminologyClient) {
		this.dataClient = dataClient;
		this.terminologyClient = terminologyClient;
	}
	
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
	
	public void setLibraryResolutionProvider(LibraryResolutionProvider<Library> provider) {
		this.libraryProvider = provider;
	}
	
	public LibraryResolutionProvider<Library> getLibraryResolutionProvider() {
		if( this.libraryProvider == null ) {
			this.libraryProvider = new RestFhirLibraryResolutionProvider(measureClient);
		}
		return this.libraryProvider;
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
	
	/**
	 * Evaluates measures for a given patient
	 * 
	 * @param patientId Patient id to evaluate measures for
	 * @param measureContexts Measure info with parameters
	 * @param evidenceOptions Evidence options impacting the returned MeasureReports 
	 * @return List of Measure Reports
	 */
	public List<MeasureReport> evaluatePatientMeasures(String patientId, List<MeasureContext> measureContexts, MeasureEvidenceOptions evidenceOptions) {
		List<MeasureReport> measureReports = new ArrayList<>();
		MeasureReport measureReport = null;
		boolean inInitialPopulation;
		for (MeasureContext measureContext: measureContexts) {
			inInitialPopulation = false;
			measureReport = evaluatePatientMeasure(measureContext, patientId, evidenceOptions);
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
	
	/**
	 * Evaluates measures for a given patient
	 * 
	 * @param patientId Patient id to evaluate measures for
	 * @param measureContexts Measure info with parameters
	 * @return List of Measure Reports
	 */
	public List<MeasureReport> evaluatePatientMeasures(String patientId, List<MeasureContext> measureContexts) {
		return evaluatePatientMeasures(patientId, measureContexts, new MeasureEvidenceOptions());
	}

	public MeasureReport evaluatePatientMeasure(MeasureContext context, String patientId, MeasureEvidenceOptions evidenceOptions) {
		MeasureReport measureReport = null;

		if (context.getMeasureId() != null) {
			measureReport = evaluatePatientMeasure(context.getMeasureId(), patientId, context.getParameters(), evidenceOptions);
		} else if (context.getIdentifier() != null) {
			measureReport = evaluatePatientMeasure(context.getIdentifier(), context.getVersion(), patientId, context.getParameters(), evidenceOptions);
		}

		return measureReport;
	}

	public MeasureReport evaluatePatientMeasure(String measureId, String patientId, Map<String, Object> parameters, MeasureEvidenceOptions evidenceOptions) {
		Measure measure = MeasureHelper.loadMeasure(measureId, getMeasureResolutionProvider());
		return evaluatePatientMeasure(measure, patientId, parameters, evidenceOptions);
	}
	
	public MeasureReport evaluatePatientMeasure(String measureId, String patientId, Map<String, Object> parameters) {
		Measure measure = MeasureHelper.loadMeasure(measureId, getMeasureResolutionProvider());
		return evaluatePatientMeasure(measure, patientId, parameters, new MeasureEvidenceOptions());
	}

	public MeasureReport evaluatePatientMeasure(Identifier identifier, String version, String patientId, Map<String, Object> parameters, MeasureEvidenceOptions evidenceOptions) {
		Measure measure =  MeasureHelper.loadMeasure(identifier,  version, getMeasureResolutionProvider());
		return evaluatePatientMeasure(measure, patientId, parameters, evidenceOptions);
	}

	public MeasureReport evaluatePatientMeasure(Measure measure, String patientId, Map<String, Object> parameters, MeasureEvidenceOptions evidenceOptions) {
		Pair<String, String> period = getMeasurementPeriodStrategy().getMeasurementPeriod();
		return evaluatePatientMeasure(measure, patientId, period.getLeft(), period.getRight(), parameters, evidenceOptions);
	}

	public MeasureReport evaluatePatientMeasure(Measure measure, String patientId, String periodStart, String periodEnd,
			Map<String, Object> parameters, MeasureEvidenceOptions evidenceOptions) {
		LibraryResolutionProvider<Library> libraryResolutionProvider = getLibraryResolutionProvider();
		LibraryLoader libraryLoader = LibraryHelper.createLibraryLoader(libraryResolutionProvider);

		EvaluationProviderFactory factory = new ProviderFactory(dataClient, terminologyClient);
		MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(factory, libraryLoader, libraryResolutionProvider);
		seeder.disableDebugLogging();

		// TODO - consider talking with OSS project about making source, user, and pass
		// a properties collection for more versatile configuration of the underlying
		// providers. For example, we need an additional custom HTTP header for
		// configuration of our FHIR server.
		IMeasureEvaluationSeed seed = seeder.create(measure, periodStart, periodEnd, "ProductLine");

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
