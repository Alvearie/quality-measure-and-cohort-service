/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Type;
import org.opencds.cqf.common.evaluation.EvaluationProviderFactory;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.fhir.model.FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;

import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.engine.measure.parameter.UnsupportedFhirTypeException;
import com.ibm.cohort.engine.measure.seed.IMeasureEvaluationSeed;
import com.ibm.cohort.engine.measure.seed.MeasureEvaluationSeeder;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Provide an interface for doing quality measure evaluation against a FHIR R4
 * server.
 */
@SuppressWarnings("rawtypes")
public class MeasureEvaluator {
	protected static final String PARAMETER_EXTENSION_URL = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter";

	private IGenericClient dataClient;
	private IGenericClient terminologyClient;
	private IGenericClient measureClient;
	private FhirModelResolver fhirModelResolver;

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
		this.fhirModelResolver = new R4FhirModelResolver();
	}

	public MeasureEvaluator(
			IGenericClient dataClient,
			IGenericClient terminologyClient,
			IGenericClient measureClient,
			FhirModelResolver fhirModelResolver) {
		this.dataClient = dataClient;
		this.terminologyClient = terminologyClient;
		this.measureClient = measureClient;
		this.fhirModelResolver = fhirModelResolver;
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
		MeasureReport measureReport;
		for (MeasureContext measureContext: measureContexts) {
			measureReport = evaluatePatientMeasure(measureContext, patientId, evidenceOptions);
			measureReports.add(measureReport);
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
		Pair<String, String> period = getMeasurementPeriodStrategy().getMeasurementPeriod(measure, parameters);
		return evaluatePatientMeasure(measure, patientId, period.getLeft(), period.getRight(), parameters, evidenceOptions);
	}

	/**
	 * Evaluate a FHIR Quality Measure for a given Patient.
	 * 
	 * The evaluate operation creates a default parameter for the CQL engine named
	 * "Measurement Period" that is populated with Interval[periodStart, true,
	 * periodEnd, true]. The <a href=
	 * "https://www.hl7.org/fhir/measure-operation-evaluate-measure.html">FHIR
	 * evaluate operation</a> defines these values as the FHIR
	 * <a href="https://www.hl7.org/fhir/datatypes.html#date">date</a> type which is
	 * a human readable expression that can be a partial date (e.g. YEAR,
	 * YEAR-MONTH, or YEAR-MONTH-DAY format). FHIR dates do not include a timezone,
	 * so the periodStart and periodEnd are interpreted as occurring in the timezone
	 * of the server evaluating the request.
	 * 
	 * @param measure         FHIR Measure resource
	 * @param patientId       FHIR resource ID of the Patient resource to use as the
	 *                        subject of the evaluation
	 * @param periodStart     FHIR date string representing the start of the
	 *                        Measurement Period.
	 * @param periodEnd       FHIR date string representing the end of the
	 *                        Measurement Period.
	 * @param parameters      override values for parameters defined in the CQL
	 *                        libraries used to evaluate the measure
	 * @param evidenceOptions Settings that control what evidence will be written
	 *                        into the MeasureReport
	 * @return FHIR MeasureReport
	 */
	public MeasureReport evaluatePatientMeasure(Measure measure, String patientId, String periodStart, String periodEnd,
			Map<String, Object> parameters, MeasureEvidenceOptions evidenceOptions) {
		LibraryResolutionProvider<Library> libraryResolutionProvider = getLibraryResolutionProvider();
		LibraryLoader libraryLoader = LibraryHelper.createLibraryLoader(libraryResolutionProvider);

		EvaluationProviderFactory factory = new ProviderFactory(dataClient, terminologyClient);
		MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(factory, libraryLoader, libraryResolutionProvider);
		seeder.disableDebugLogging();
		
		IMeasureEvaluationSeed seed = seeder.create(measure, periodStart, periodEnd, "ProductLine");

		measure.getExtension().stream()
				.filter(this::isParameterExtension)
				.forEach(measureDefault ->
						         seed.getContext().setParameter(
								         null,
								         measureDefault.getId(),
								         toCqlObject(measureDefault.getValue())));

		if (parameters != null) {
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				seed.getContext().setParameter(null, entry.getKey(), entry.getValue());
			}
		}

		CDMMeasureEvaluation evaluation = new CDMMeasureEvaluation(seed.getDataProvider(), seed.getMeasurementPeriod());
		return evaluation.evaluatePatientMeasure(measure, seed.getContext(), patientId);
	}

	private Object toCqlObject(Type type) {
		return Optional.ofNullable(type)
				.map((fhirType) -> fhirModelResolver.resolvePath(fhirType, "value"))
				.orElseThrow(() -> new UnsupportedFhirTypeException(type));
	}

    private boolean isParameterExtension(Extension extension) {
        return PARAMETER_EXTENSION_URL.equals(extension.getUrl());
    }
}
