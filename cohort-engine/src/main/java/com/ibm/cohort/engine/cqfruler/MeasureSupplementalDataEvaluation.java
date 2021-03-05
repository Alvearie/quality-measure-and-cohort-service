/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 * 
 * Originated from org.opencds.cqf.r4.evaluation.MeasureEvaluation
 */

package com.ibm.cohort.engine.cqfruler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Code;

public class MeasureSupplementalDataEvaluation {
	
	private MeasureSupplementalDataEvaluation() {}
	
	public static void populateSDEAccumulators(Measure measure, Context context, Patient patient,
			Map<String, Map<String, Integer>> sdeAccumulators,
			List<Measure.MeasureSupplementalDataComponent> sde) {
		context.setContextValue("Patient", patient.getIdElement().getIdPart());
		List<Object> sdeList = sde.stream()
				.map(sdeItem -> context.resolveExpressionRef(sdeItem.getCriteria().getExpression()).evaluate(context))
				.collect(Collectors.toList());
		if (!sdeList.isEmpty()) {
			for (int i = 0; i < sdeList.size(); i++) {
				Object sdeListItem = sdeList.get(i);
				if (null != sdeListItem) {
					String sdeAccumulatorKey = sde.get(i).getCode().getText();
					if (null == sdeAccumulatorKey || sdeAccumulatorKey.length() < 1) {
						sdeAccumulatorKey = sde.get(i).getCriteria().getExpression();
					}
					Map<String, Integer> sdeItemMap = sdeAccumulators.get(sdeAccumulatorKey);
					String code = "";

					switch (sdeListItem.getClass().getSimpleName()) {
					case "Code":
						code = ((Code) sdeListItem).getCode();
						break;
					case "ArrayList":
						if (((ArrayList<?>) sdeListItem).isEmpty()) {
							code = ((Coding) ((ArrayList<?>) sdeListItem).get(0)).getCode();
						} else {
							continue;
						}
						break;
					}
					if (null == code) {
						continue;
					}
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

	public static MeasureReport processAccumulators(MeasureReport report,
			Map<String, Map<String, Integer>> sdeAccumulators,
			List<Measure.MeasureSupplementalDataComponent> sde, boolean isSingle, List<Patient> patients) {
		List<Reference> newRefList = new ArrayList<>();
		sdeAccumulators.forEach((sdeKey, sdeAccumulator) -> {
			sdeAccumulator.forEach((sdeAccumulatorKey, sdeAccumulatorValue) -> {
				Observation obs = new Observation();
				obs.setStatus(Observation.ObservationStatus.FINAL);
				obs.setId(UUID.randomUUID().toString());
				Coding valueCoding = new Coding();
				if (sdeKey.equalsIgnoreCase("sde-sex")) {
					valueCoding.setCode(sdeAccumulatorKey);
				} else {
					String coreCategory = sdeKey.substring(sdeKey.lastIndexOf('-'));
					patients.forEach((pt) -> {
						pt.getExtension().forEach((ptExt) -> {
							if (ptExt.getUrl().contains(coreCategory)) {
								String code = ((Coding) ptExt.getExtension().get(0).getValue()).getCode();
								if (code.equalsIgnoreCase(sdeAccumulatorKey)) {
									valueCoding
											.setSystem(((Coding) ptExt.getExtension().get(0).getValue()).getSystem());
									valueCoding.setCode(code);
									valueCoding
											.setDisplay(((Coding) ptExt.getExtension().get(0).getValue()).getDisplay());
								}
							}
						});
					});
				}
				CodeableConcept obsCodeableConcept = new CodeableConcept();
				Extension obsExtension = new Extension()
						.setUrl("http://hl7.org/fhir/StructureDefinition/cqf-measureInfo");
				Extension extExtMeasure = new Extension().setUrl("measure")
						.setValue(new CanonicalType("http://hl7.org/fhir/us/cqfmeasures/" + report.getMeasure()));
				obsExtension.addExtension(extExtMeasure);
				Extension extExtPop = new Extension().setUrl("populationId").setValue(new StringType(sdeKey));
				obsExtension.addExtension(extExtPop);
				obs.addExtension(obsExtension);
				obs.setValue(new IntegerType(sdeAccumulatorValue));
				if (!isSingle) {
					valueCoding.setCode(sdeAccumulatorKey);
					obsCodeableConcept.setCoding(Collections.singletonList(valueCoding));
					obs.setCode(obsCodeableConcept);
				} else {
					obs.setCode(new CodeableConcept().setText(sdeKey));
					obsCodeableConcept.setCoding(Collections.singletonList(valueCoding));
					obs.setValue(obsCodeableConcept);
				}
				newRefList.add(new Reference("#" + obs.getId()));
				report.addContained(obs);
			});
		});
		newRefList.addAll(report.getEvaluatedResource());
		report.setEvaluatedResource(newRefList);
		return report;
	}
}
