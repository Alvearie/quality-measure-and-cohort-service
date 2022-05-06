/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure;

import static com.ibm.cohort.cql.cdm.CDMConstants.MEASURE_PARAMETER_VALUE_URL;
import static com.ibm.cohort.cql.cdm.CDMConstants.PARAMETER_VALUE_URL;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ibm.cohort.measure.wrapper.BaseWrapper;
import com.ibm.cohort.measure.wrapper.WrapperFactory;
import com.ibm.cohort.measure.wrapper.element.ExtensionWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureGroupPopulationWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureGroupWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureReportGroupPopulationWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureReportGroupWrapper;
import com.ibm.cohort.measure.wrapper.element.ParameterDefinitionWrapper;
import com.ibm.cohort.measure.wrapper.enums.MeasureReportType;
import com.ibm.cohort.measure.wrapper.enums.ParameterUse;
import com.ibm.cohort.measure.wrapper.resource.MeasureReportWrapper;
import com.ibm.cohort.measure.wrapper.resource.MeasureWrapper;
import com.ibm.cohort.measure.wrapper.resource.ResourceWrapper;
import com.ibm.cohort.measure.wrapper.type.BooleanWrapper;
import com.ibm.cohort.measure.wrapper.type.StringWrapper;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
//import org.hl7.fhir.r4.model.BooleanType;
//import org.hl7.fhir.r4.model.Extension;
//import org.hl7.fhir.r4.model.Measure;
//import org.hl7.fhir.r4.model.MeasureReport;
//import org.hl7.fhir.r4.model.ParameterDefinition;
//import org.hl7.fhir.r4.model.StringType;
//import org.hl7.fhir.r4.model.Type;
//import org.hl7.fhir.r4.model.codesystems.MeasureScoring;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;
import org.opencds.cqf.common.evaluation.MeasureScoring;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.ibm.cohort.cql.cdm.CDMConstants;
import com.ibm.cohort.measure.cqfruler.CDMContext;
import com.ibm.cohort.measure.cqfruler.MeasureEvaluation;
import com.ibm.cohort.measure.evidence.MeasureEvidenceHelper;
import com.ibm.cohort.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.measure.evidence.MeasureEvidenceOptions.DefineReturnOptions;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;

/**
 * Implementation of measure evaluation logic for the IBM Common Data Model IG
 * Patient Quality Measure profile.
 */
public class CDMMeasureEvaluation {

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
				MeasureReportGroupWrapper reportGroup) {
			StandardReportResults idx = new StandardReportResults();
			for (MeasureReportGroupPopulationWrapper pop : reportGroup.getPopulation()) {
				MeasurePopulationType standardType = MeasurePopulationType
						.fromCode(pop.getCode().getCoding().get(0).getCode());
				if (standardType != null) {
					idx.put(standardType, pop.getCount() > 0);
				}
			}
			return idx;
		}
	}

	private MeasureEvaluation evaluation;
	private WrapperFactory wrapperFactory;

	public CDMMeasureEvaluation(DataProvider provider, Interval measurementPeriod, WrapperFactory wrapperFactory) {
		this.wrapperFactory = wrapperFactory;
		this.evaluation = new MeasureEvaluation(provider, measurementPeriod, wrapperFactory);
	}

	/**
	 * Evaluate a CDM Patient Quality Measure
	 * 
	 * @param measure   CDM Patient Quality Measure
	 * @param context   CQL Engine Execution Context pre-configured for use in
	 *                  measure evaluation
	 * @param patientIds Patient ID(s) of the patient to evaluate (first element when generating INDIVIDUAL type report)
	 * @param evidenceOptions MeasureEvidenceOptions to indicate whether or not to return evaluated resources and define level results
     * @param parameterMap Map of parameter names to Parameter objects
	 * @param type  Type of report to be generated
	 * @return MeasureReport with population components filled out.
	 */
	public MeasureReportWrapper evaluatePatientMeasure(MeasureWrapper measure, Context context, List<String> patientIds, MeasureEvidenceOptions evidenceOptions, Map<String, Parameter> parameterMap, MeasureReportType type) {
		context.setExpressionCaching(true);

		boolean includeEvaluatedResources = (evidenceOptions != null) ? evidenceOptions.isIncludeEvaluatedResources() : false;

		MeasureReportWrapper report;
		switch (type) {
			case INDIVIDUAL:
				report = evaluation.evaluatePatientMeasure(measure, context, patientIds.get(0), includeEvaluatedResources);
				break;
			case SUBJECT_LIST:
				report = evaluation.evaluatePatientListMeasure(measure, context, patientIds, includeEvaluatedResources);
				break;
			default:
				throw new IllegalStateException("Unsupported measure report type requested: " + type);
		}

		setReportMeasureToMeasureId(report, measure);

		MeasureScoring scoring = MeasureScoring.fromCode(measure.getScoringCode());
		switch (scoring) {
			case PROPORTION:
			case RATIO:
				// implement custom logic for CDM care-gaps
				Iterator<MeasureGroupWrapper> it = measure.getGroup().iterator();
				for (int i = 0; it.hasNext(); i++) {
					MeasureGroupWrapper group = it.next();
					MeasureReportGroupWrapper reportGroup = report.getGroup().get(i);
					boolean evaluateCareGaps = isEligibleForCareGapEvaluation(reportGroup);

					for (MeasureGroupPopulationWrapper pop : group.getPopulation()) {
						if (pop.getCode().hasCoding(CDMConstants.CDM_CODE_SYSTEM_MEASURE_POPULATION_TYPE, CDMConstants.CARE_GAP)) {
							Boolean result = Boolean.FALSE;
							if (evaluateCareGaps) {
								result = evaluateCriteria(context, pop.getExpression());
							}

							MeasureReportGroupPopulationWrapper output = wrapperFactory.newMeasureReportGroupPopulation();
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
		
		if(context instanceof CDMContext) {
			CDMContext defineContext = (CDMContext) context;
			
			// Grab the define results from the expression cache
			MeasureEvidenceOptions.DefineReturnOptions defineReturnOptions = (evidenceOptions != null ) ? evidenceOptions.getDefineReturnOption() : MeasureEvidenceOptions.DefineReturnOptions.NONE;
			addDefineEvaluationToReport(report, defineContext, defineReturnOptions);
		}

		List<ExtensionWrapper> parameterExtensions = getParameterExtensions(measure, context, parameterMap);
		parameterExtensions.forEach(report::addExtension);

		return report;
	}
	
	protected void addDefineEvaluationToReport(MeasureReportWrapper report, CDMContext defineContext, DefineReturnOptions defineOption) {
		if(DefineReturnOptions.NONE == defineOption) {
			return;
		}
		
		for(Entry<VersionedIdentifier, Map<String, Object>> libraryCache : defineContext.getEntriesInCache()) {
			for(Entry<String, Object> defineResult : libraryCache.getValue().entrySet()) {
				
				List<BaseWrapper> values = MeasureEvidenceHelper.getFhirTypes(defineResult.getValue(), wrapperFactory);
				
				if (shouldAddDefineResult(defineOption, values)) {
					
					ExtensionWrapper evidence = wrapperFactory.newExtension();
					evidence.setUrl(CDMConstants.EVIDENCE_URL);

					StringWrapper key = wrapperFactory.newString();
					key.setValue(MeasureEvidenceHelper.createEvidenceKey(libraryCache.getKey(), defineResult.getKey()));

					ExtensionWrapper textExtension = wrapperFactory.newExtension();
					textExtension.setUrl(CDMConstants.EVIDENCE_TEXT_URL);
					textExtension.setValue(key);
					
					evidence.addExtension(textExtension);
					
					for(BaseWrapper value : values) {
						ExtensionWrapper valueExtension = wrapperFactory.newExtension();
						valueExtension.setUrl(CDMConstants.EVIDENCE_VALUE_URL);
						valueExtension.setValue(value);
						evidence.addExtension(valueExtension);
					}
					
					report.addExtension(evidence);
				}
			}
		}
	}
	
	protected List<ExtensionWrapper> getParameterExtensions(MeasureWrapper measure, Context context, Map<String, Parameter> parameterMap) {
		Set<String> parameterNames = new HashSet<>();
		
		// Check for special parameters we handle elsewhere
		if (context.resolveParameterRef(null, CDMConstants.MEASUREMENT_PERIOD) != null) {
			parameterNames.add(CDMConstants.MEASUREMENT_PERIOD);
		}
		
		if (context.resolveParameterRef(null, CDMConstants.PRODUCT_LINE) != null) {
			parameterNames.add(CDMConstants.PRODUCT_LINE);
		}

		if (parameterMap != null) {
			parameterNames.addAll(parameterMap.keySet());
		}
		
		List<ExtensionWrapper> parameterExtensions = measure.getExtensionsByUrl(CDMConstants.MEASURE_PARAMETER_URL);
		for (ExtensionWrapper e : parameterExtensions) {
			ParameterDefinitionWrapper parameterDefinition = (ParameterDefinitionWrapper) e.getValue();
			parameterNames.add(parameterDefinition.getName());
		}
		
		return parameterNames.stream()
				.map(x -> createParameterExtension(context, x))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	protected ExtensionWrapper createParameterExtension(Context context, String parameterName) {
		Object parameterValue = context.resolveParameterRef(null, parameterName);

		ExtensionWrapper innerExtension = wrapperFactory.newExtension();
		innerExtension.setUrl(PARAMETER_VALUE_URL);
		CQLToFHIRMeasureReportHelper helper = new CQLToFHIRMeasureReportHelper(wrapperFactory);
		BaseWrapper fhirParameterValue = helper.getFhirTypeValue(parameterValue);

		ExtensionWrapper outerExtension = null;

		// Do not create an extension for unsupported types
		if (fhirParameterValue != null) {
			innerExtension.setValue(fhirParameterValue);

			ParameterDefinitionWrapper parameterDefinition = wrapperFactory.newParameterDefinition();
			parameterDefinition.setName(parameterName);
			parameterDefinition.setUse(ParameterUse.IN);
			parameterDefinition.setExtension(Collections.singletonList(innerExtension));
			parameterDefinition.setType(fhirParameterValue.fhirType());

			outerExtension = wrapperFactory.newExtension();
			outerExtension.setUrl(MEASURE_PARAMETER_VALUE_URL);
			outerExtension.setValue(parameterDefinition);
		}
		
		return outerExtension;
	}

	/**
	 * Set the measure reference on a MeasureReport to the id of a given Measure.
	 * Attempt to normalize the id to the format "Measure/ID" if possible.
	 *
	 * Examples:
	 *   Measure/id1234 stays Measure/id1234
	 *   Measure/id1234/_history/10 stays Measure/id1234/_history/10
	 *   http://fhir-server-url/api/v4/Measure/id5 becomes Measure/id5
	 *   http://fhir-server-url/api/v4/Measure/id7/_history/4 becomes Measure/id7/_history/4
	 *
	 * If the id does not contain "Measure/", then the report measure is set to the
	 * full id of the measure.
	 *
	 * Example:
	 *   id55432 stays id55432
	 *
	 * @param report MeasureReport on which to set the measure reference
	 * @param measure Measure providing the id to normalize and set on the report
	 */
	protected static void setReportMeasureToMeasureId(MeasureReportWrapper report, MeasureWrapper measure) {
		int startOfId = measure.getId().indexOf("Measure/");

		if (startOfId < 0) {
			report.setMeasure(measure.getId());
		} else {
			report.setMeasure(measure.getId().substring(startOfId));
		}
	}

	private static boolean shouldAddDefineResult(DefineReturnOptions defineOption, List<BaseWrapper> values) {
		if(!values.isEmpty()) {
			if(DefineReturnOptions.ALL == defineOption) {
				return true;
			}
			else if(DefineReturnOptions.BOOLEAN == defineOption
					&& values.size() == 1
					&& values.get(0) instanceof BooleanWrapper) {
				return true;
			}
		}
		
		return false;
	}
	
	protected void addBooleanDefineEvaluationToReport(MeasureReportWrapper report, CDMContext defineContext) {
		for(Entry<VersionedIdentifier, Map<String, Object>> libraryCache : defineContext.getEntriesInCache()) {
			for(Entry<String, Object> defineResult : libraryCache.getValue().entrySet()) {

				BaseWrapper value = wrapperFactory.wrapObject(defineResult.getValue());
				
				if (value instanceof BooleanWrapper) {
					
					ExtensionWrapper evidence = wrapperFactory.newExtension();
					evidence.setUrl(CDMConstants.EVIDENCE_URL);
					
					StringWrapper key = wrapperFactory.newString();
					key.setValue(MeasureEvidenceHelper.createEvidenceKey(libraryCache.getKey(), defineResult.getKey()));
					
					ExtensionWrapper textExtension = wrapperFactory.newExtension();
					textExtension.setUrl(CDMConstants.EVIDENCE_TEXT_URL);
					textExtension.setValue(key);
					
					evidence.addExtension(textExtension);
					
					ExtensionWrapper valueExtension = wrapperFactory.newExtension();
					valueExtension.setUrl(CDMConstants.EVIDENCE_VALUE_URL);
					valueExtension.setValue(value);
					evidence.addExtension(valueExtension);
					
					report.addExtension(evidence);
				}
			}
		}
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
	private boolean isEligibleForCareGapEvaluation(MeasureReportGroupWrapper reportGroup) {
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
		Object result = context.resolveExpressionRef(expression).evaluate(context);
		if (result == null) {
			result = Collections.emptyList();
		}

		if (result instanceof Boolean) {
			return (Boolean) result;
		} else if (result instanceof List) {
			return !((List<?>) result).isEmpty();
		} else {
			throw new IllegalArgumentException(String
					.format("Criteria expression '%s' did not evaluate to a boolean or list result.", expression));
		}
	}
}
