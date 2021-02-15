/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.codesystems.MeasureScoring;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.ibm.cohort.engine.cqfruler.MeasureEvaluation;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;

/**
 * Implementation of measure evaluation logic for the IBM Common Data Model IG
 * Patient Quality Measure profile.
 */
public class CDMMeasureEvaluation {

	public static final String CARE_GAP = "care-gap";
	public static final String CDM_CODE_SYSTEM_MEASURE_POPULATION_TYPE = "http://ibm.com/fhir/cdm/CodeSystem/measure-population-type";
	
	/**
	 * Helper for collecting and indexing the various standard population types from
	 * base FHIR and their count values so that they can easily be referenced in the
	 * business logic.
	 */
	public static final class StandardReportResults extends HashMap<MeasurePopulationType, Boolean> {

		private static final long serialVersionUID = 1L;

		public boolean inInitialPopulation() {
			return inPopulation(MeasurePopulationType.INITIALPOPULATION);
		}

		public boolean inDenominator() {
			return inPopulation(MeasurePopulationType.DENOMINATOR);
		}

		public boolean inDenominatorExclusion() {
			return inPopulation(MeasurePopulationType.DENOMINATOREXCLUSION);
		}

		public boolean inDenominatorException() {
			return inPopulation(MeasurePopulationType.DENOMINATOREXCEPTION);
		}

		public boolean inNumerator() {
			return inPopulation(MeasurePopulationType.NUMERATOR);
		}

		public boolean inNumeratorExclusion() {
			return inPopulation(MeasurePopulationType.NUMERATOREXCLUSION);
		}

		protected boolean inPopulation(MeasurePopulationType type) {
			Boolean b = get(type);
			if (b != null) {
				return b.booleanValue();
			} else {
				return false;
			}
		}

		public static StandardReportResults fromMeasureReportGroup(
				MeasureReport.MeasureReportGroupComponent reportGroup) {
			StandardReportResults idx = new StandardReportResults();
			for (MeasureReport.MeasureReportGroupPopulationComponent pop : reportGroup.getPopulation()) {
				MeasurePopulationType standardType = MeasurePopulationType
						.fromCode(pop.getCode().getCodingFirstRep().getCode());
				if (standardType != null) {
					idx.put(standardType, pop.getCount() > 0);
				}
			}
			return idx;
		}
	}

	private MeasureEvaluation evaluation;

	public CDMMeasureEvaluation(DataProvider provider, Interval measurementPeriod) {
		evaluation = new MeasureEvaluation(provider, measurementPeriod);
	}

	/**
	 * Evaluate a CDM Patient Quality Measure
	 * 
	 * @param measure   CDM Patient Quality Measure
	 * @param context   CQL Engine Execution Context pre-configured for use in
	 *                  measure evaluation
	 * @param patientId Patient ID of the patient to evaluate
	 * @return MeasureReport with population components filled out.
	 */
	public MeasureReport evaluatePatientMeasure(Measure measure, Context context, String patientId, MeasureEvidenceOptions evidenceOptions) {
		context.setExpressionCaching(true);
		MeasureReport report = evaluation.evaluatePatientMeasure(measure, context, patientId, evidenceOptions);

		MeasureScoring scoring = MeasureScoring.fromCode(measure.getScoring().getCodingFirstRep().getCode());
		switch (scoring) {
		case PROPORTION:
		case RATIO:
			// implement custom logic for CDM care-gaps
			Iterator<Measure.MeasureGroupComponent> it = measure.getGroup().iterator();
			for (int i = 0; it.hasNext(); i++) {
				Measure.MeasureGroupComponent group = it.next();
				MeasureReport.MeasureReportGroupComponent reportGroup = report.getGroup().get(i);
				boolean evaluateCareGaps = isEligibleForCareGapEvaluation(reportGroup);

				for (Measure.MeasureGroupPopulationComponent pop : group.getPopulation()) {
					if (pop.getCode().hasCoding(CDM_CODE_SYSTEM_MEASURE_POPULATION_TYPE, CARE_GAP)) {
						Boolean result = Boolean.FALSE;
						if (evaluateCareGaps) {
							result = evaluateCriteria(context, pop.getCriteria().getExpression());
						}

						MeasureReport.MeasureReportGroupPopulationComponent output = new MeasureReport.MeasureReportGroupPopulationComponent();
						output.setId(pop.getId()); // need this to differentiate between multiple instances of care-gap
						output.setCode(pop.getCode());
						output.setCount(result ? 1 : 0);
						reportGroup.addPopulation(output);
					}
				}
			}
			break;
		default:
			// no customizations needed
		}

		return report;
	}

	/**
	 * Given the results in a report group determine whether or not the patient is
	 * eligible for care gap evaluation. Care gaps are applied after all the normal
	 * report logic with all of the normal report rules about initial-population,
	 * numerator, and denominator.
	 * 
	 * @param reportGroup Report group containing population results for standard
	 *                    patient quality measure reporting.
	 * @return true when care gaps should be evaluated, otherwise false.
	 */
	private boolean isEligibleForCareGapEvaluation(MeasureReport.MeasureReportGroupComponent reportGroup) {
		boolean isEligibleForCareGap = false;
		StandardReportResults results = StandardReportResults.fromMeasureReportGroup(reportGroup);
		// Logic for the numerator exclusion, denominator exclusion, and denominator
		// exception has already been applied by the standard report generator and the
		// patient has been removed from the following populations as needed, so we can
		// keep this logic simple.
		if (results.inDenominator() && !results.inNumerator()) {
			isEligibleForCareGap = true;
		}
		return isEligibleForCareGap;
	}

	/**
	 * Evaluate the criteria expression
	 * 
	 * @param context    CQL Engine Execution Context
	 * @param expression Which expression in the CQL library to evaluate. Expression
	 *                   must evaluate to a Boolean or List result.
	 * @return result of the expression if the result type was a boolean or
	 *         true/false when list result and count > 0.
	 */
	private Boolean evaluateCriteria(Context context, String expression) {
		// TODO: Determine why the OSS implementation clears the expression cache after
		// each evaluation. That seems to be generally a bad thing unless
		// the population components are coming from different libraries with
		// potentially overlapping define names, but we _know_ that isn't happening
		// because we are following the Davinci spec where there is only one Library
		// entry point.

		Object result = context.resolveExpressionRef(expression).evaluate(context);
		if (result == null) {
			result = Collections.emptyList();
		}

		if (result instanceof Boolean) {
			return (Boolean) result;
		} else if (result instanceof List) {
			return ((List<?>) result).size() > 0;
		} else {
			throw new IllegalArgumentException(String
					.format("Criteria expression '%s' did not evaluate to a boolean or list result.", expression));
		}
	}
}
