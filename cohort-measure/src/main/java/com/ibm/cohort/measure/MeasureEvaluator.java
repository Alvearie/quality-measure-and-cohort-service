/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.MeasureReport;

import com.ibm.cohort.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.measure.seed.IMeasureEvaluationSeed;
import com.ibm.cohort.measure.seed.MeasureEvaluationSeeder;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;

/**
 * Provide an interface for doing quality measure evaluation against a FHIR R4
 * server.
 */
public class MeasureEvaluator<L, M, PD, I> {

	private final MeasureEvaluationSeeder<L, M, PD, I> seeder;
//	private final FhirResourceResolver<M> measureResolver;
	private final MeasureHelper<M> measureHelper;
	private MeasurementPeriodStrategy measurementPeriodStrategy;

	public MeasureEvaluator(
			MeasureEvaluationSeeder<L, M, PD, I> seeder,
			MeasureHelper<M> measureHelper
//			FhirResourceResolver<M> measureResolver
	) {
		this.seeder = seeder;
		this.measureHelper = measureHelper;
//		this.measureResolver = measureResolver;
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
			measureReport = evaluatePatientMeasure(patientId, measureContext, evidenceOptions);
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
	
	public MeasureReport evaluatePatientMeasure(String patientId, MeasureContext context, MeasureEvidenceOptions evidenceOptions) {
		MeasureReport measureReport = null;

		if (context.getMeasureId() != null) {
			measureReport = evaluatePatientMeasure(context.getMeasureId(), patientId, context.getParameters(), evidenceOptions);
		} else if (context.getIdentifier() != null) {
			measureReport = evaluatePatientMeasure(context.getIdentifier(), context.getVersion(), patientId, context.getParameters(), evidenceOptions);
		}

		return measureReport;
	}

	public MeasureReport evaluatePatientMeasure(String measureId, String patientId, Map<String, Parameter> parameters, MeasureEvidenceOptions evidenceOptions) {
		M measure = measureHelper.loadMeasure(measureId);
		return evaluatePatientMeasure(measure, patientId, parameters, evidenceOptions);
	}
	
	public MeasureReport evaluatePatientMeasure(String measureId, String patientId, Map<String, Parameter> parameters) {
		return evaluatePatientMeasure(measureId, patientId, parameters, new MeasureEvidenceOptions());
	}

	public MeasureReport evaluatePatientMeasure(Identifier identifier, String version, String patientId, Map<String, Parameter> parameters, MeasureEvidenceOptions evidenceOptions) {
		M measure =  measureHelper.loadMeasure(identifier,  version);
		return evaluatePatientMeasure(measure, patientId, parameters, evidenceOptions);
	}

	public MeasureReport evaluatePatientMeasure(M measure, String patientId, Map<String, Parameter> parameters, MeasureEvidenceOptions evidenceOptions) {
		Pair<String, String> period = getMeasurementPeriodStrategy().getMeasurementPeriod(parameters);
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
	public MeasureReport evaluatePatientMeasure(M measure, String patientId, String periodStart, String periodEnd,
			Map<String, Parameter> parameters, MeasureEvidenceOptions evidenceOptions) {
//		MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(terminologyProvider, dataProviders, libraryDependencyGatherer, libraryResolver);
//		seeder.disableDebugLogging();

		IMeasureEvaluationSeed seed = seeder.create(measure, periodStart, periodEnd, "ProductLine", parameters);

		CDMMeasureEvaluation evaluation = new CDMMeasureEvaluation(seed.getDataProvider(), seed.getMeasurementPeriod());
		return evaluation.evaluatePatientMeasure(measure, seed.getContext(), Collections.singletonList(patientId), evidenceOptions, parameters, MeasureReport.MeasureReportType.INDIVIDUAL);
	}

	public MeasureReport evaluatePatientListMeasure(
			List<String> patientIds,
			MeasureContext measureContext,
			MeasureEvidenceOptions evidenceOptions) {
		M measure = measureHelper.loadMeasure(measureContext);

		return evaluatePatientListMeasure(patientIds, measure, measureContext.getParameters(), evidenceOptions);
	}

	public MeasureReport evaluatePatientListMeasure(
			List<String> patientIds,
			M measure,
			Map<String, Parameter> parameters,
			MeasureEvidenceOptions evidenceOptions) {
//		MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(terminologyProvider, dataProviders, libraryDependencyGatherer, libraryResolver);
		seeder.disableDebugLogging();

		Pair<String, String> period = getMeasurementPeriodStrategy().getMeasurementPeriod(parameters);

		IMeasureEvaluationSeed seed = seeder.create(measure, period.getLeft(), period.getRight(), "ProductLine", parameters);

		CDMMeasureEvaluation evaluation = new CDMMeasureEvaluation(seed.getDataProvider(), seed.getMeasurementPeriod());
		return evaluation.evaluatePatientMeasure(measure, seed.getContext(), patientIds, evidenceOptions, parameters, MeasureReport.MeasureReportType.SUBJECTLIST);
	}
}
