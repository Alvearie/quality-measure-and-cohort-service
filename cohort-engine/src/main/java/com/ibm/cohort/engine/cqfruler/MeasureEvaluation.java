/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 * 
 * Originated from org.opencds.cqf.r4.evaluation.MeasureEvaluation
 */

package com.ibm.cohort.engine.cqfruler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.cqframework.cql.elm.execution.ExpressionDef;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.ListResource.ListEntryComponent;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;
import org.opencds.cqf.common.evaluation.MeasureScoring;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.engine.r4.builder.MeasureReportBuilder;

public class MeasureEvaluation {

    private static final Logger logger = LoggerFactory.getLogger(MeasureEvaluation.class);

    private DataProvider provider;
    private Interval measurementPeriod;

    public MeasureEvaluation(DataProvider provider, Interval measurementPeriod) {
        this.provider = provider;
        this.measurementPeriod = measurementPeriod;
    }

    public MeasureReport evaluatePatientMeasure(Measure measure, Context context, String patientId) {
    	return evaluatePatientMeasure(measure, context, patientId, new MeasureEvidenceOptions());
    }
    
    public MeasureReport evaluatePatientMeasure(Measure measure, Context context, String patientId, MeasureEvidenceOptions evidenceOptions) {
        logger.info("Generating individual report");

        if (patientId == null) {
            throw new IllegalArgumentException("Must provide patient id");
        }

        Iterable<Object> patientRetrieve = provider.retrieve("Patient", "id", patientId, "Patient", null, null, null,
                null, null, null, null, null);
        Patient patient = null;
        if (patientRetrieve.iterator().hasNext()) {
            patient = (Patient) patientRetrieve.iterator().next();
        }

        boolean isSingle = true;
        return evaluate(measure, context,
                patient == null ? Collections.emptyList() : Collections.singletonList(patient),
                MeasureReport.MeasureReportType.INDIVIDUAL, isSingle, evidenceOptions.isIncludeEvaluatedResources());
    }

    @SuppressWarnings("unchecked")
    private Iterable<Resource> evaluateCriteria(Context context, Patient patient,
            Measure.MeasureGroupPopulationComponent pop, boolean includeEvidence) {
        if (pop == null || !pop.hasCriteria()) {
            return Collections.emptyList();
        }

        context.setContextValue("Patient", patient.getIdElement().getIdPart());

        ExpressionDef populationExpressionDef = context.resolveExpressionRef(pop.getCriteria().getExpression());
        Object result = populationExpressionDef.evaluate(context);
        
        if (result == null) {
            return Collections.emptyList();
        }

        if (result instanceof Boolean) {
            if (((Boolean) result)) {
                return Collections.singletonList(patient);
            } else {
                return Collections.emptyList();
            }
        }

        return (Iterable<Resource>) result;
    }

    private boolean evaluatePopulationCriteria(Context context, Patient patient,
            Measure.MeasureGroupPopulationComponent criteria, Map<String, Resource> population,
            Map<String, Patient> populationPatients, Measure.MeasureGroupPopulationComponent exclusionCriteria,
            Map<String, Resource> exclusionPopulation, Map<String, Patient> exclusionPatients,
            boolean includeEvidence) {
        
    	boolean inPopulation = false;
        if (criteria != null) {
            for (Resource resource : evaluateCriteria(context, patient, criteria, includeEvidence)) {
                inPopulation = true;
                population.put(resource.getIdElement().getIdPart(), resource);
            }
        }

        if (inPopulation) {
            // Are they in the exclusion?
            if (exclusionCriteria != null) {
                for (Resource resource : evaluateCriteria(context, patient, exclusionCriteria, includeEvidence)) {
                    inPopulation = false;
                    exclusionPopulation.put(resource.getIdElement().getIdPart(), resource);
                    population.remove(resource.getIdElement().getIdPart());
                }
            }
        }

        if (inPopulation && populationPatients != null) {
            populationPatients.put(patient.getIdElement().getIdPart(), patient);
        }
        if (!inPopulation && exclusionPatients != null) {
            exclusionPatients.put(patient.getIdElement().getIdPart(), patient);
        }

        return inPopulation;
    }

    private void addPopulationCriteriaReport(MeasureReport report,
            MeasureReport.MeasureReportGroupComponent reportGroup,
            Measure.MeasureGroupPopulationComponent populationCriteria, int populationCount,
            Iterable<Patient> patientPopulation) {
        if (populationCriteria != null) {
            MeasureReport.MeasureReportGroupPopulationComponent populationReport = new MeasureReport.MeasureReportGroupPopulationComponent();
            populationReport.setCode(populationCriteria.getCode());
            if (report.getType() == MeasureReport.MeasureReportType.SUBJECTLIST && patientPopulation != null) {
                ListResource subjectList = new ListResource();
                subjectList.setId(UUID.randomUUID().toString());
                populationReport.setSubjectResults(new Reference().setReference("#" + subjectList.getId()));
                for (Patient patient : patientPopulation) {
                    ListResource.ListEntryComponent entry = new ListResource.ListEntryComponent()
                            .setItem(new Reference()
                                    .setReference(patient.getIdElement().getIdPart().startsWith("Patient/")
                                            ? patient.getIdElement().getIdPart()
                                            : String.format("Patient/%s", patient.getIdElement().getIdPart()))
                                    .setDisplay(patient.getNameFirstRep().getNameAsSingleString()));
                    subjectList.addEntry(entry);
                }
                report.addContained(subjectList);
            }
            populationReport.setCount(populationCount);
            reportGroup.addPopulation(populationReport);
        }
    }

    private MeasureReport evaluate(Measure measure, Context context, List<Patient> patients,
            MeasureReport.MeasureReportType type, boolean isSingle, boolean includeEvidence) {
        MeasureReportBuilder reportBuilder = new MeasureReportBuilder();
        reportBuilder.buildStatus("complete");
        reportBuilder.buildType(type);
        reportBuilder.buildMeasureReference(
                measure.getIdElement().getResourceType() + "/" + measure.getIdElement().getIdPart());
        if (type == MeasureReport.MeasureReportType.INDIVIDUAL && !patients.isEmpty()) {
            IdType patientId = patients.get(0).getIdElement();
            reportBuilder.buildPatientReference(patientId.getResourceType() + "/" + patientId.getIdPart());
        }
        reportBuilder.buildPeriod(measurementPeriod);

        MeasureReport report = reportBuilder.build();

        Map<String, Resource> resources = new HashMap<>();
        Map<String, Set<String>> codeToResourceMap = new HashMap<>();

        MeasureScoring measureScoring = MeasureScoring.fromCode(measure.getScoring().getCodingFirstRep().getCode());
        if (measureScoring == null) {
            throw new RuntimeException("Measure scoring is required in order to calculate.");
        }

        List<Measure.MeasureSupplementalDataComponent> sde = new ArrayList<>();
        Map<String, Map<String, Integer>> sdeAccumulators = null;
        for (Measure.MeasureGroupComponent group : measure.getGroup()) {
            MeasureReport.MeasureReportGroupComponent reportGroup = new MeasureReport.MeasureReportGroupComponent();
            reportGroup.setId(group.getId());
            report.getGroup().add(reportGroup);

            // Declare variables to avoid a hash lookup on every patient
            // TODO: Isn't quite right, there may be multiple initial populations for a
            // ratio measure...
            Measure.MeasureGroupPopulationComponent initialPopulationCriteria = null;
            Measure.MeasureGroupPopulationComponent numeratorCriteria = null;
            Measure.MeasureGroupPopulationComponent numeratorExclusionCriteria = null;
            Measure.MeasureGroupPopulationComponent denominatorCriteria = null;
            Measure.MeasureGroupPopulationComponent denominatorExclusionCriteria = null;
            Measure.MeasureGroupPopulationComponent denominatorExceptionCriteria = null;
            Measure.MeasureGroupPopulationComponent measurePopulationCriteria = null;
            Measure.MeasureGroupPopulationComponent measurePopulationExclusionCriteria = null;
//            // TODO: Isn't quite right, there may be multiple measure observations...
            Measure.MeasureGroupPopulationComponent measureObservationCriteria = null;

            Map<String, Resource> initialPopulation = null;
            Map<String, Resource> numerator = null;
            Map<String, Resource> numeratorExclusion = null;
            Map<String, Resource> denominator = null;
            Map<String, Resource> denominatorExclusion = null;
            Map<String, Resource> denominatorException = null;
            Map<String, Resource> measurePopulation = null;
            Map<String, Resource> measurePopulationExclusion = null;
            Map<String, Resource> measureObservation = null;

            Map<String, Patient> initialPopulationPatients = null;
            Map<String, Patient> numeratorPatients = null;
            Map<String, Patient> numeratorExclusionPatients = null;
            Map<String, Patient> denominatorPatients = null;
            Map<String, Patient> denominatorExclusionPatients = null;
            Map<String, Patient> denominatorExceptionPatients = null;
            Map<String, Patient> measurePopulationPatients = null;
            Map<String, Patient> measurePopulationExclusionPatients = null;

            for (Measure.MeasureGroupPopulationComponent pop : group.getPopulation()) {
                MeasurePopulationType populationType = MeasurePopulationType
                        .fromCode(pop.getCode().getCodingFirstRep().getCode());
                if (populationType != null) {
                    switch (populationType) {
                        case INITIALPOPULATION:
                            initialPopulationCriteria = pop;
                            initialPopulation = new HashMap<>();
                            if (type == MeasureReport.MeasureReportType.SUBJECTLIST) {
                                initialPopulationPatients = new HashMap<>();
                            }
                            break;
                        case NUMERATOR:
                            numeratorCriteria = pop;
                            numerator = new HashMap<>();
                            if (type == MeasureReport.MeasureReportType.SUBJECTLIST) {
                                numeratorPatients = new HashMap<>();
                            }
                            break;
                        case NUMERATOREXCLUSION:
                            numeratorExclusionCriteria = pop;
                            numeratorExclusion = new HashMap<>();
                            if (type == MeasureReport.MeasureReportType.SUBJECTLIST) {
                                numeratorExclusionPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOR:
                            denominatorCriteria = pop;
                            denominator = new HashMap<>();
                            if (type == MeasureReport.MeasureReportType.SUBJECTLIST) {
                                denominatorPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOREXCLUSION:
                            denominatorExclusionCriteria = pop;
                            denominatorExclusion = new HashMap<>();
                            if (type == MeasureReport.MeasureReportType.SUBJECTLIST) {
                                denominatorExclusionPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOREXCEPTION:
                            denominatorExceptionCriteria = pop;
                            denominatorException = new HashMap<>();
                            if (type == MeasureReport.MeasureReportType.SUBJECTLIST) {
                                denominatorExceptionPatients = new HashMap<>();
                            }
                            break;
                        case MEASUREPOPULATION:
                            measurePopulationCriteria = pop;
                            measurePopulation = new HashMap<>();
                            if (type == MeasureReport.MeasureReportType.SUBJECTLIST) {
                                measurePopulationPatients = new HashMap<>();
                            }
                            break;
                        case MEASUREPOPULATIONEXCLUSION:
                            measurePopulationExclusionCriteria = pop;
                            measurePopulationExclusion = new HashMap<>();
                            if (type == MeasureReport.MeasureReportType.SUBJECTLIST) {
                                measurePopulationExclusionPatients = new HashMap<>();
                            }
                            break;
                        case MEASUREOBSERVATION:
                            measureObservationCriteria = pop;
                            measureObservation = new HashMap<>();
                            break;
                    }
                }
            }

            sdeAccumulators = new HashMap<>();
            sde = measure.getSupplementalData();
            
            switch (measureScoring) {
                case PROPORTION:
                case RATIO: {

                    // For each patient in the initial population
                    for (Patient patient : patients) {
                        // Are they in the initial population?
                        boolean inInitialPopulation = evaluatePopulationCriteria(context, patient,
                                initialPopulationCriteria, initialPopulation, initialPopulationPatients, null, null,
                                null, includeEvidence);
                        populateResourceMap(context, MeasurePopulationType.INITIALPOPULATION, resources,
                                codeToResourceMap, includeEvidence);

                        if (inInitialPopulation) {
                            // Are they in the denominator?
                            boolean inDenominator = evaluatePopulationCriteria(context, patient, denominatorCriteria,
                                    denominator, denominatorPatients, denominatorExclusionCriteria,
                                    denominatorExclusion, denominatorExclusionPatients, includeEvidence);
                            populateResourceMap(context, MeasurePopulationType.DENOMINATOR, resources,
                                    codeToResourceMap, includeEvidence);

                            if (inDenominator) {
                                // Are they in the numerator?
                                boolean inNumerator = evaluatePopulationCriteria(context, patient, numeratorCriteria,
                                        numerator, numeratorPatients, numeratorExclusionCriteria, numeratorExclusion,
                                        numeratorExclusionPatients, includeEvidence);
                                populateResourceMap(context, MeasurePopulationType.NUMERATOR, resources,
                                        codeToResourceMap, includeEvidence);

                                if (!inNumerator && inDenominator && (denominatorExceptionCriteria != null)) {
                                    // Are they in the denominator exception?
                                    boolean inException = false;
                                    for (Resource resource : evaluateCriteria(context, patient,
                                            denominatorExceptionCriteria, includeEvidence)) {
                                        inException = true;
                                        denominatorException.put(resource.getIdElement().getIdPart(), resource);
                                        denominator.remove(resource.getIdElement().getIdPart());
                                        populateResourceMap(context, MeasurePopulationType.DENOMINATOREXCEPTION,
                                                resources, codeToResourceMap, includeEvidence);
                                    }
                                    if (inException) {
                                        if (denominatorExceptionPatients != null) {
                                            denominatorExceptionPatients.put(patient.getIdElement().getIdPart(),
                                                    patient);
                                        }
                                        if (denominatorPatients != null) {
                                            denominatorPatients.remove(patient.getIdElement().getIdPart());
                                        }
                                    }
                                }
                            }
                        }
                        MeasureSupplementalDataEvaluation.populateSDEAccumulators(measure, context, patient, sdeAccumulators, sde);
                    }

                    // Calculate actual measure score, Count(numerator) / Count(denominator)
                    if (denominator != null && numerator != null && denominator.size() > 0) {
                        reportGroup.setMeasureScore(new Quantity(numerator.size() / (double) denominator.size()));
                    }

                    break;
                }
                case CONTINUOUSVARIABLE: {

                    // For each patient in the patient list
                    for (Patient patient : patients) {

                        // Are they in the initial population?
                        boolean inInitialPopulation = evaluatePopulationCriteria(context, patient,
                                initialPopulationCriteria, initialPopulation, initialPopulationPatients, null, null,
                                null, includeEvidence);
                        populateResourceMap(context, MeasurePopulationType.INITIALPOPULATION, resources,
                                codeToResourceMap, includeEvidence);

                        if (inInitialPopulation) {
                            // Are they in the measure population?
                            boolean inMeasurePopulation = evaluatePopulationCriteria(context, patient,
                                    measurePopulationCriteria, measurePopulation, measurePopulationPatients,
                                    measurePopulationExclusionCriteria, measurePopulationExclusion,
                                    measurePopulationExclusionPatients, includeEvidence);

                            if (inMeasurePopulation) {
                                for (Resource resource : measurePopulation.values()) {
                                    Resource observation = ObservationMeasureEvaluation.evaluateObservationCriteria(context, patient, resource, measureObservationCriteria, report);
                                    measureObservation.put(resource.getIdElement().getIdPart(), observation);
                                    report.addContained(observation);
                                    report.getEvaluatedResource().add(new Reference("#" + observation.getId()));
                                }
                            }
                        }
                        MeasureSupplementalDataEvaluation.populateSDEAccumulators(measure, context, patient, sdeAccumulators,sde);
                    }

                    break;
                }
                case COHORT: {

                    // For each patient in the patient list
                    for (Patient patient : patients) {
                        evaluatePopulationCriteria(context, patient,
                                initialPopulationCriteria, initialPopulation, initialPopulationPatients, null, null,
                                null, includeEvidence);
                        populateResourceMap(context, MeasurePopulationType.INITIALPOPULATION, resources,
                                codeToResourceMap, includeEvidence);
                        MeasureSupplementalDataEvaluation.populateSDEAccumulators(measure, context, patient, sdeAccumulators, sde);
                    }

                    break;
                }
                
            }

            // Add population reports for each group
            addPopulationCriteriaReport(report, reportGroup, initialPopulationCriteria,
                    initialPopulation != null ? initialPopulation.size() : 0,
                    initialPopulationPatients != null ? initialPopulationPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, numeratorCriteria,
                    numerator != null ? numerator.size() : 0,
                    numeratorPatients != null ? numeratorPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, numeratorExclusionCriteria,
                    numeratorExclusion != null ? numeratorExclusion.size() : 0,
                    numeratorExclusionPatients != null ? numeratorExclusionPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, denominatorCriteria,
                    denominator != null ? denominator.size() : 0,
                    denominatorPatients != null ? denominatorPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, denominatorExclusionCriteria,
                    denominatorExclusion != null ? denominatorExclusion.size() : 0,
                    denominatorExclusionPatients != null ? denominatorExclusionPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, denominatorExceptionCriteria,
                    denominatorException != null ? denominatorException.size() : 0,
                    denominatorExceptionPatients != null ? denominatorExceptionPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, measurePopulationCriteria,
                    measurePopulation != null ? measurePopulation.size() : 0,
                    measurePopulationPatients != null ? measurePopulationPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, measurePopulationExclusionCriteria,
                    measurePopulationExclusion != null ? measurePopulationExclusion.size() : 0,
                    measurePopulationExclusionPatients != null ? measurePopulationExclusionPatients.values() : null);
            // TODO: Measure Observations...
        }

        for (Entry<String, Set<String>> entry : codeToResourceMap.entrySet()) {
            ListResource list = new ListResource();
            
            for (String element : entry.getValue()) {
                ListResource.ListEntryComponent comp = new ListEntryComponent();
                comp.setItem(new Reference('#' + element));
                list.addEntry(comp);
            }

            if (!list.isEmpty()) {
                list.setId(UUID.randomUUID().toString());
                list.setTitle(entry.getKey());
                resources.put(list.getId(), list);
            }
        }

        if (!resources.isEmpty()) {
            List<Reference> evaluatedResourceIds = new ArrayList<>();
            resources.forEach((key, resource) -> {
                evaluatedResourceIds.add(new Reference(resource.getId()));
            });
            report.setEvaluatedResource(evaluatedResourceIds);
        }
        if (sdeAccumulators.size() > 0) {
            report = MeasureSupplementalDataEvaluation.processAccumulators(report, sdeAccumulators, sde, isSingle, patients);
        }

        return report;
    }

    private void populateResourceMap(Context context, MeasurePopulationType type, Map<String, Resource> resources,
            Map<String, Set<String>> codeToResourceMap, boolean includeEvidence) {
        if (context.getEvaluatedResources().isEmpty()) {
            return;
        }

        if(!includeEvidence) {
        	return;
        }
        
        if (!codeToResourceMap.containsKey(type.toCode())) {
            codeToResourceMap.put(type.toCode(), new HashSet<>());
        }

        Set<String> codeHashSet = codeToResourceMap.get((type.toCode()));

        for (Object o : context.getEvaluatedResources()) {
            if (o instanceof Resource) {
                Resource r = (Resource) o;
                String id = (r.getIdElement().getResourceType() != null ? (r.getIdElement().getResourceType() + "/")
                        : "") + r.getIdElement().getIdPart();
                if (!codeHashSet.contains(id)) {
                    codeHashSet.add(id);
                }

                if (!resources.containsKey(id)) {
                    resources.put(id, r);
                }
            }
        }

        context.clearEvaluatedResources();
    }
}