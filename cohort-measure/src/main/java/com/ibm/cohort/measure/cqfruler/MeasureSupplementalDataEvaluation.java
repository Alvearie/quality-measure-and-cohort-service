/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.cqfruler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ibm.cohort.measure.wrapper.enums.ObservationStatus;
import com.ibm.cohort.measure.wrapper.WrapperFactory;
import com.ibm.cohort.measure.wrapper.element.CodeableConceptWrapper;
import com.ibm.cohort.measure.wrapper.element.CodingWrapper;
import com.ibm.cohort.measure.wrapper.element.ExtensionWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureSupplementalDataWrapper;
import com.ibm.cohort.measure.wrapper.element.ReferenceWrapper;
import com.ibm.cohort.measure.wrapper.resource.MeasureReportWrapper;
import com.ibm.cohort.measure.wrapper.resource.ObservationWrapper;
import com.ibm.cohort.measure.wrapper.resource.PatientWrapper;
import com.ibm.cohort.measure.wrapper.type.CanonicalWrapper;
import com.ibm.cohort.measure.wrapper.type.IntegerWrapper;
import com.ibm.cohort.measure.wrapper.type.StringWrapper;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Code;

public class MeasureSupplementalDataEvaluation {
	
	public static final String SDE_SEX = "sde-sex";
	public static final String CQF_MEASUREINFO_URL = "http://hl7.org/fhir/StructureDefinition/cqf-measureInfo";
	public static final String CQFMEASURES_URL = "http://hl7.org/fhir/us/cqfmeasures/";
	public static final String POPULATION_ID = "populationId";
	public static final String MEASURE = "measure";
	
	private MeasureSupplementalDataEvaluation() {}
	
	public static void populateSDEAccumulators(Context context, PatientWrapper patient,
			Map<String, Map<String, Integer>> sdeAccumulators,
			List<MeasureSupplementalDataWrapper> sde,
			WrapperFactory wrapperFactory) {
		
		context.setContextValue(MeasureEvaluation.PATIENT, patient.getId());
		
		List<Object> sdeList = sde.stream()
				.map(sdeItem -> context.resolveExpressionRef(sdeItem.getExpression()).evaluate(context))
				.collect(Collectors.toList());
		if (!sdeList.isEmpty()) {
			for (int i = 0; i < sdeList.size(); i++) {
				Object sdeListItem = sdeList.get(i);
				if (null != sdeListItem) {
					String sdeAccumulatorKey = sde.get(i).getCode();
					if (null == sdeAccumulatorKey || sdeAccumulatorKey.length() < 1) {
						sdeAccumulatorKey = sde.get(i).getExpression();
					}
					Map<String, Integer> sdeItemMap = sdeAccumulators.get(sdeAccumulatorKey);
					String code = null;

					switch (sdeListItem.getClass().getSimpleName()) {
					case "Code":
						code = ((Code) sdeListItem).getCode();
						break;
					case "ArrayList":
						ArrayList<?> rawList = (ArrayList<?>) sdeListItem;
						if (!rawList.isEmpty()) {
							CodingWrapper coding = wrapperFactory.wrapObject(rawList.get(0));
							code = coding.getCode();
						}
						break;
					default: 
						throw new UnsupportedOperationException("Supplemental data evaluation not supported for type: " + sdeListItem.getClass());
					}
					
					if (null != code) {
						if (null != sdeItemMap && null != sdeItemMap.get(code)) {
							Integer sdeItemValue = sdeItemMap.get(code);
							sdeItemValue++;
							sdeItemMap.put(code, sdeItemValue);
							sdeAccumulators.get(sdeAccumulatorKey).put(code, sdeItemValue);
						} else {
							if (null == sdeAccumulators.get(sdeAccumulatorKey)) {
								HashMap<String, Integer> newSDEItem = new HashMap<>();
								newSDEItem.put(code, 1);
								sdeAccumulators.put(sdeAccumulatorKey, newSDEItem);
							} else {
								sdeAccumulators.get(sdeAccumulatorKey).put(code, 1);
							}
						}
					}
				}
			}
		}
	}

	public static MeasureReportWrapper processAccumulators(MeasureReportWrapper report,
			Map<String, Map<String, Integer>> sdeAccumulators, boolean isSingle, List<PatientWrapper> patients,
			WrapperFactory wrapperFactory) {
		List<ReferenceWrapper> newRefList = new ArrayList<>();
		sdeAccumulators.forEach((sdeKey, sdeAccumulator) -> {
			sdeAccumulator.forEach((sdeAccumulatorKey, sdeAccumulatorValue) -> {
				ObservationWrapper obs = wrapperFactory.newObservation();
				obs.setStatus(ObservationStatus.FINAL);
				obs.setId(UUID.randomUUID().toString());
				CodingWrapper valueCoding = wrapperFactory.newCoding();
				if (sdeKey.equalsIgnoreCase(SDE_SEX)) {
					valueCoding.setCode(sdeAccumulatorKey);
				} else {
					String coreCategory = sdeKey.substring(sdeKey.lastIndexOf('-'));
					patients.forEach((pt) -> {
						pt.getExtension().forEach((ptExt) -> {
							if (ptExt.getUrl().contains(coreCategory)) {
								CodingWrapper coding = (CodingWrapper) ptExt.getExtension().get(0).getValue();
								String code = coding.getCode();
								if (code.equalsIgnoreCase(sdeAccumulatorKey)) {
									valueCoding
											.setSystem(coding.getSystem());
									valueCoding.setCode(code);
									valueCoding
											.setDisplay(coding.getDisplay());
								}
							}
						});
					});
				}
				CodeableConceptWrapper obsCodeableConcept = wrapperFactory.newCodeableConcept();
				ExtensionWrapper obsExtension = wrapperFactory.newExtension();
				obsExtension.setUrl(CQF_MEASUREINFO_URL);
				ExtensionWrapper extExtMeasure = wrapperFactory.newExtension();
				extExtMeasure.setUrl(MEASURE);
				CanonicalWrapper canonicalWrapper = wrapperFactory.newCanonical();
				canonicalWrapper.setValue(CQFMEASURES_URL + report.getMeasure());
				extExtMeasure.setValue(canonicalWrapper);
				obsExtension.addExtension(extExtMeasure);
				ExtensionWrapper extExtPop = wrapperFactory.newExtension();
				extExtPop.setUrl(POPULATION_ID);
				StringWrapper string = wrapperFactory.newString();
				string.setValue(sdeKey);
				extExtPop.setValue(string);
				obsExtension.addExtension(extExtPop);
				obs.addExtension(obsExtension);
				IntegerWrapper integer = wrapperFactory.newInteger();
				integer.setValue(sdeAccumulatorValue);
				obs.setValue(integer);
				if (!isSingle) {
					valueCoding.setCode(sdeAccumulatorKey);
					obsCodeableConcept.setCoding(Collections.singletonList(valueCoding));
					obs.setCode(obsCodeableConcept);
				} else {
					CodeableConceptWrapper sdeCodeableConcept = wrapperFactory.newCodeableConcept();
					sdeCodeableConcept.setText(sdeKey);
					obs.setCode(sdeCodeableConcept);
					obsCodeableConcept.setCoding(Collections.singletonList(valueCoding));
					obs.setValue(obsCodeableConcept);
				}
				ReferenceWrapper reference = wrapperFactory.newReference();
				reference.setReference("#" + obs.getId());
				newRefList.add(reference);
				report.addContained(obs);
			});
		});
		newRefList.addAll(report.getEvaluatedResource());
		report.setEvaluatedResource(newRefList);
		return report;
	}
}
