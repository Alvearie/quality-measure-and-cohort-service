/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.cqfruler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.ibm.cohort.measure.MeasureReportType;
import com.ibm.cohort.measure.wrapper.element.ListEntryWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureGroupPopulationWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureGroupWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureReportGroupPopulationWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureReportGroupWrapper;
import com.ibm.cohort.measure.wrapper.element.ReferenceWrapper;
import com.ibm.cohort.measure.wrapper.resource.ListWrapper;
import com.ibm.cohort.measure.wrapper.resource.MeasureReportWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureSupplementalDataWrapper;
import com.ibm.cohort.measure.wrapper.resource.MeasureWrapper;
import com.ibm.cohort.measure.wrapper.resource.PatientWrapper;
import com.ibm.cohort.measure.wrapper.resource.ResourceWrapper;
import com.ibm.cohort.measure.wrapper.WrapperFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.cqframework.cql.elm.execution.ExpressionDef;
//import org.hl7.fhir.r4.model.IdType;
//import org.hl7.fhir.r4.model.ListResource;
//import org.hl7.fhir.r4.model.ListResource.ListEntryComponent;
//import org.hl7.fhir.r4.model.Measure;
//import org.hl7.fhir.r4.model.MeasureReport;
//import org.hl7.fhir.r4.model.Patient;
//import org.hl7.fhir.r4.model.Quantity;
//import org.hl7.fhir.r4.model.Reference;
//import org.hl7.fhir.r4.model.Resource;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;
import org.opencds.cqf.common.evaluation.MeasureScoring;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * KWAS BIG TODO:
 *           Make some form of measure report builder interface (and an implementation for r4).
 *           Make a measurereportgroupcomponent builder too???
 */

public class MeasureEvaluation {

    private static final Logger logger = LoggerFactory.getLogger(MeasureEvaluation.class);

    public static final String PATIENT = "Patient";

    private DataProvider provider;
    private Interval measurementPeriod;

//    private final IdFieldHandler<R> resourceHandler;
//    private final PatientFieldHandler<P> patientHandler;
//    private final MeasureReportFieldHandler<MR> measureReportHandler;
//    private final MeasureFieldHandler<M, I> measureHandler;

    private final WrapperFactory wrapperFactory;

    public MeasureEvaluation(DataProvider provider, Interval measurementPeriod) {
        this.provider = provider;
        this.measurementPeriod = measurementPeriod;
    }

    public MeasureReportWrapper evaluatePatientMeasure(MeasureWrapper measure, Context context, String patientId) {
    	return evaluatePatientMeasure(measure, context, patientId, false);
    }

    public MeasureReportWrapper evaluatePatientMeasure(MeasureWrapper measure, Context context, String patientId, boolean includeEvaluatedResources) {
        logger.info("Generating individual report");

        if (patientId == null) {
            throw new IllegalArgumentException("Must provide patient id");
        }

        Iterable<Object> patientRetrieve = provider.retrieve(PATIENT, "id", patientId, PATIENT, null, null, null,
                null, null, null, null, null);
        PatientWrapper patient = null;
        if (patientRetrieve.iterator().hasNext()) {
            Object rawPatient = patientRetrieve.iterator().next();
            patient = wrapperFactory.wrapResource(rawPatient);
        }

        boolean isSingle = true;
        return evaluate(measure, context,
                patient == null ? Collections.emptyList() : Collections.singletonList(patient),
                MeasureReportType.INDIVIDUAL, isSingle, includeEvaluatedResources);
    }

    public MeasureReportWrapper evaluatePatientListMeasure(MeasureWrapper measure, Context context, List<String> patientIds, boolean includeEvaluatedResources) {
	    logger.info("Generating patient-list report");

	    List<PatientWrapper> patients = toPatients(patientIds);
	    boolean isSingle = false;
	    return evaluate(measure, context, patients, MeasureReportType.SUBJECT_LIST, isSingle, includeEvaluatedResources);
    }

	private List<PatientWrapper> toPatients(List<String> patientIds) {
		List<PatientWrapper> patients = new ArrayList<>();

		for (String patientId : patientIds) {
			Iterable<Object> patientRetrieve = provider.retrieve(PATIENT, "id", patientId, PATIENT, null, null, null, null, null, null, null, null);
			if (patientRetrieve.iterator().hasNext()) {
                Object rawPatient = patientRetrieve.iterator().next();
                PatientWrapper patientWrapper = wrapperFactory.wrapResource(rawPatient);
                patients.add(patientWrapper);
			}
		}

		return patients;
	}

	@SuppressWarnings("unchecked")
    private List<ResourceWrapper> evaluateCriteria(Context context, PatientWrapper patient,
                                                       MeasureGroupPopulationWrapper pop) {
        if (pop == null || !pop.hasCriteria()) {
            return Collections.emptyList();
        }

        context.setContextValue(PATIENT, patient.getId());

        ExpressionDef populationExpressionDef = context.resolveExpressionRef(pop.getExpression());
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

        Iterable<Object> iterableResult = (Iterable<Object>)result;
        List<ResourceWrapper> retVal = new ArrayList<>();
        for (Object object : iterableResult) {
            retVal.add(wrapperFactory.wrapResource(object));
        }

        return retVal;
    }

    protected boolean evaluatePopulationCriteria(Context context, PatientWrapper patient,
            MeasureGroupPopulationWrapper criteria, Map<String, ResourceWrapper> population,
            Map<String, PatientWrapper> populationPatients, MeasureGroupPopulationWrapper exclusionCriteria,
            Map<String, ResourceWrapper> exclusionPopulation, Map<String, PatientWrapper> exclusionPatients) {
        
    	boolean inPopulation = false;
        if (criteria != null) {
            for (ResourceWrapper resource : evaluateCriteria(context, patient, criteria)) {
                inPopulation = true;
                population.put(resource.getId(), resource);
            }
        }

        if (inPopulation) {
            // Are they in the exclusion?
            if (exclusionCriteria != null) {
                for (ResourceWrapper resource : evaluateCriteria(context, patient, exclusionCriteria)) {
                    inPopulation = false;
                    exclusionPopulation.put(resource.getId(), resource);
                    population.remove(resource.getId());
                }
            }
        }

        if (inPopulation && populationPatients != null) {
            populationPatients.put(patient.getId(), patient);
        }
        if (!inPopulation && exclusionPatients != null) {
            exclusionPatients.put(patient.getId(), patient);
        }

        return inPopulation;
    }

    private void addPopulationCriteriaReport(MeasureReportWrapper report,
            MeasureReportType reportType,
            MeasureReportGroupWrapper reportGroup,
            MeasureGroupPopulationWrapper populationCriteria, int populationCount,
            Iterable<PatientWrapper> patientPopulation) {
        if (populationCriteria != null) {
            MeasureReportGroupPopulationWrapper populationReport = wrapperFactory.newMeasureReportGroupPopulation();
            populationReport.setCode(populationCriteria.getCode());
            if (reportType == MeasureReportType.SUBJECT_LIST && patientPopulation != null) {
                String listId = UUID.randomUUID().toString();
                ListWrapper subjectList = wrapperFactory.newList();
                subjectList.setId(listId);
                ReferenceWrapper listReference = wrapperFactory.newReference();
                listReference.setReference(listId);
                populationReport.setSubjectResults(listReference);
                for (PatientWrapper patient : patientPopulation) {
                    String patientId = patient.getId().startsWith("Patient/")
                            ? patient.getId()
                            : String.format("Patient/%s", patient.getId());

                    ReferenceWrapper patientReference = wrapperFactory.newReference();
                    patientReference.setReference(patientId);

                    ListEntryWrapper subjectListEntry = wrapperFactory.newListEntry();
                    subjectListEntry.setItem(patientReference);

                    subjectList.addEntry(subjectListEntry);
                }
                report.addContained(subjectList);

            }
            populationReport.setCount(populationCount);
            reportGroup.addPopulation(populationReport);
        }
    }

    protected MeasureReportWrapper evaluate(MeasureWrapper measure, Context context, List<PatientWrapper> patients,
                                            MeasureReportType type, boolean isSingle, boolean includeEvaluatedResources) {
        MeasureReportWrapper retVal = wrapperFactory.newMeasureReport();
        retVal.setStatus("complete");
        retVal.setType(type);
        retVal.setMeasure(measure.getResourceType() + "/" + measure.getId());
        if (type == MeasureReportType.INDIVIDUAL && CollectionUtils.isNotEmpty(patients)) {
            PatientWrapper patient = patients.get(0);
            ReferenceWrapper patientReference = wrapperFactory.newReference();
            patientReference.setReference(patient.getResourceType() + "/" + patient.getId());
            retVal.setSubject(patientReference);
        }
        retVal.setPeriod(measurementPeriod);

        Map<String, ResourceWrapper> resources = new HashMap<>();
        Map<String, Set<String>> codeToResourceMap = new HashMap<>();

        MeasureScoring measureScoring = MeasureScoring.fromCode(measure.getScoringCode());
        if (measureScoring == null) {
            throw new RuntimeException("Measure scoring is required in order to calculate.");
        }

        List<MeasureSupplementalDataWrapper> sde = measure.getSupplementalData();
        Map<String, Map<String, Integer>> sdeAccumulators = new HashMap<>();
        
        for (MeasureGroupWrapper group : measure.getGroup()) {
            MeasureReportGroupWrapper reportGroup = wrapperFactory.newMeasureReportGroup();
            reportGroup.setId(group.getId());
            retVal.addGroup(reportGroup);

            // Declare variables to avoid a hash lookup on every patient
            // TODO: Isn't quite right, there may be multiple initial populations for a
            // ratio measure...
            MeasureGroupPopulationWrapper initialPopulationCriteria = null;
            MeasureGroupPopulationWrapper numeratorCriteria = null;
            MeasureGroupPopulationWrapper numeratorExclusionCriteria = null;
            MeasureGroupPopulationWrapper denominatorCriteria = null;
            MeasureGroupPopulationWrapper denominatorExclusionCriteria = null;
            MeasureGroupPopulationWrapper denominatorExceptionCriteria = null;

            Map<String, ResourceWrapper> initialPopulation = null;
            Map<String, ResourceWrapper> numerator = null;
            Map<String, ResourceWrapper> numeratorExclusion = null;
            Map<String, ResourceWrapper> denominator = null;
            Map<String, ResourceWrapper> denominatorExclusion = null;
            Map<String, ResourceWrapper> denominatorException = null;

            Map<String, PatientWrapper> initialPopulationPatients = null;
            Map<String, PatientWrapper> numeratorPatients = null;
            Map<String, PatientWrapper> numeratorExclusionPatients = null;
            Map<String, PatientWrapper> denominatorPatients = null;
            Map<String, PatientWrapper> denominatorExclusionPatients = null;
            Map<String, PatientWrapper> denominatorExceptionPatients = null;

            for (MeasureGroupPopulationWrapper pop : group.getPopulation()) {
                MeasurePopulationType populationType = MeasurePopulationType.fromCode(pop.getCode());
                if (populationType != null) {
                    switch (populationType) {
                        case INITIALPOPULATION:
                            initialPopulationCriteria = pop;
                            initialPopulation = new HashMap<>();
                            if (type == MeasureReportType.SUBJECT_LIST) {
                                initialPopulationPatients = new HashMap<>();
                            }
                            break;
                        case NUMERATOR:
                            numeratorCriteria = pop;
                            numerator = new HashMap<>();
                            if (type == MeasureReportType.SUBJECT_LIST) {
                                numeratorPatients = new HashMap<>();
                            }
                            break;
                        case NUMERATOREXCLUSION:
                            numeratorExclusionCriteria = pop;
                            numeratorExclusion = new HashMap<>();
                            if (type == MeasureReportType.SUBJECT_LIST) {
                                numeratorExclusionPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOR:
                            denominatorCriteria = pop;
                            denominator = new HashMap<>();
                            if (type == MeasureReportType.SUBJECT_LIST) {
                                denominatorPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOREXCLUSION:
                            denominatorExclusionCriteria = pop;
                            denominatorExclusion = new HashMap<>();
                            if (type == MeasureReportType.SUBJECT_LIST) {
                                denominatorExclusionPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOREXCEPTION:
                            denominatorExceptionCriteria = pop;
                            denominatorException = new HashMap<>();
                            if (type == MeasureReportType.SUBJECT_LIST) {
                                denominatorExceptionPatients = new HashMap<>();
                            }
                            break;
                        default:
                        	throw new UnsupportedOperationException("Measure population, observation and measure population exclusion are used for continuous-variable scoring measures which are not supported");
                    }
                }
            }
            
            switch (measureScoring) {
                case PROPORTION:
                case RATIO: {

                    // For each patient in the initial population
                    for (PatientWrapper patient : patients) {
                        // Are they in the initial population?
                        boolean inInitialPopulation = evaluatePopulationCriteria(context, patient,
                                initialPopulationCriteria, initialPopulation, initialPopulationPatients, null, null,
                                null);
                        populateResourceMap(context, MeasurePopulationType.INITIALPOPULATION, resources,
                                codeToResourceMap, includeEvaluatedResources);

                        if (inInitialPopulation) {
                            // Are they in the denominator?
                            boolean inDenominator = evaluatePopulationCriteria(context, patient, denominatorCriteria,
                                    denominator, denominatorPatients, denominatorExclusionCriteria,
                                    denominatorExclusion, denominatorExclusionPatients);
                            populateResourceMap(context, MeasurePopulationType.DENOMINATOR, resources,
                                    codeToResourceMap, includeEvaluatedResources);

                            if (inDenominator) {
                                // Are they in the numerator?
                                boolean inNumerator = evaluatePopulationCriteria(context, patient, numeratorCriteria,
                                        numerator, numeratorPatients, numeratorExclusionCriteria, numeratorExclusion,
                                        numeratorExclusionPatients);
                                populateResourceMap(context, MeasurePopulationType.NUMERATOR, resources,
                                        codeToResourceMap, includeEvaluatedResources);

                                if (!inNumerator && inDenominator && (denominatorExceptionCriteria != null)) {
                                    // Are they in the denominator exception?
                                    boolean inException = false;
                                    for (ResourceWrapper resource : evaluateCriteria(context, patient,
                                            denominatorExceptionCriteria)) {
                                        inException = true;
                                        denominatorException.put(resource.getId(), resource);
                                        denominator.remove(resource.getId());
                                        populateResourceMap(context, MeasurePopulationType.DENOMINATOREXCEPTION,
                                                resources, codeToResourceMap, includeEvaluatedResources);
                                    }
                                    if (inException) {
                                        if (denominatorExceptionPatients != null) {
                                            denominatorExceptionPatients.put(patient.getId(),
                                                    patient);
                                        }
                                        if (denominatorPatients != null) {
                                            denominatorPatients.remove(patient.getId());
                                        }
                                    }
                                }
                            }
                        }
                        MeasureSupplementalDataEvaluation.populateSDEAccumulators(context, patient, sdeAccumulators, sde);
                    }

                    // Calculate actual measure score, Count(numerator) / Count(denominator)
                    if (numerator != null && MapUtils.isNotEmpty(denominator)) {
                        reportGroup.setScoreAsQuantity(numerator.size() / (double) denominator.size());
                    }

                    break;
                }
                case COHORT: {

                    // For each patient in the patient list
                    for (PatientWrapper patient : patients) {
                        evaluatePopulationCriteria(context, patient,
                                initialPopulationCriteria, initialPopulation, initialPopulationPatients, null, null,
                                null);
                        populateResourceMap(context, MeasurePopulationType.INITIALPOPULATION, resources,
                                codeToResourceMap, includeEvaluatedResources);
                        MeasureSupplementalDataEvaluation.populateSDEAccumulators(context, patient, sdeAccumulators, sde);
                    }

                    break;
                }
                case CONTINUOUSVARIABLE:
                	throw new UnsupportedOperationException("Scoring type CONTINUOUSVARIABLE is not supported");
                
            }

            // Add population reports for each group
            addPopulationCriteriaReport(retVal, type, reportGroup, initialPopulationCriteria,
                    initialPopulation != null ? initialPopulation.size() : 0,
                    initialPopulationPatients != null ? initialPopulationPatients.values() : null);
            addPopulationCriteriaReport(retVal, type, reportGroup, numeratorCriteria,
                    numerator != null ? numerator.size() : 0,
                    numeratorPatients != null ? numeratorPatients.values() : null);
            addPopulationCriteriaReport(retVal, type, reportGroup, numeratorExclusionCriteria,
                    numeratorExclusion != null ? numeratorExclusion.size() : 0,
                    numeratorExclusionPatients != null ? numeratorExclusionPatients.values() : null);
            addPopulationCriteriaReport(retVal, type, reportGroup, denominatorCriteria,
                    denominator != null ? denominator.size() : 0,
                    denominatorPatients != null ? denominatorPatients.values() : null);
            addPopulationCriteriaReport(retVal, type, reportGroup, denominatorExclusionCriteria,
                    denominatorExclusion != null ? denominatorExclusion.size() : 0,
                    denominatorExclusionPatients != null ? denominatorExclusionPatients.values() : null);
            addPopulationCriteriaReport(retVal, type, reportGroup, denominatorExceptionCriteria,
                    denominatorException != null ? denominatorException.size() : 0,
                    denominatorExceptionPatients != null ? denominatorExceptionPatients.values() : null);
        }

        for (Entry<String, Set<String>> entry : codeToResourceMap.entrySet()) {
            ListWrapper list = wrapperFactory.newList();

            if (!entry.getValue().isEmpty()) {
                for (String element : entry.getValue()) {
                    ReferenceWrapper reference = wrapperFactory.newReference();
                    reference.setReference('#' + element);

                    ListEntryWrapper listEntry = wrapperFactory.newListEntry();
                    listEntry.setItem(reference);
                    list.addEntry(listEntry);
                }

                String listId = UUID.randomUUID().toString();
                list.setId(listId);
                list.setTitle(entry.getKey());
                resources.put(listId, list);
            }
        }

        if (MapUtils.isNotEmpty(resources)) {
            List<ReferenceWrapper> evaluatedResourceIds = new ArrayList<>();
            resources.forEach((key, resource) -> {
                ReferenceWrapper reference = wrapperFactory.newReference();
                reference.setReference(resource.getId());
                evaluatedResourceIds.add(reference);
            });
            retVal.setEvaluatedResource(evaluatedResourceIds);
        }
        if (MapUtils.isNotEmpty(sdeAccumulators)) {
            retVal = MeasureSupplementalDataEvaluation.processAccumulators(retVal, sdeAccumulators, isSingle, patients);
        }

        return retVal;
    }

    protected void populateResourceMap(Context context, MeasurePopulationType type, Map<String, ResourceWrapper> resources,
            Map<String, Set<String>> codeToResourceMap, boolean includeEvaluatedResources) {
        if (CollectionUtils.isEmpty(context.getEvaluatedResources())) {
            return;
        }

        if(!includeEvaluatedResources) {
        	return;
        }
        
        if (!codeToResourceMap.containsKey(type.toCode())) {
            codeToResourceMap.put(type.toCode(), new HashSet<>());
        }

        Set<String> codeHashSet = codeToResourceMap.get((type.toCode()));

        for (Object o : context.getEvaluatedResources()) {
            ResourceWrapper resourceWrapper = wrapperFactory.wrapResource(o);
            if (resourceWrapper != null) {
                String id = (resourceWrapper.getResourceType() != null ? (resourceWrapper.getResourceType() + "/")
                        : "") + resourceWrapper.getId();
                if (!codeHashSet.contains(id)) {
                    codeHashSet.add(id);
                }

                if (!resources.containsKey(id)) {
                    resources.put(id, resourceWrapper);
                }
            }
        }

        context.clearEvaluatedResources();
    }
}