/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper;

import com.ibm.cohort.measure.wrapper.element.CodeableConceptWrapper;
import com.ibm.cohort.measure.wrapper.element.CodingWrapper;
import com.ibm.cohort.measure.wrapper.element.ExtensionWrapper;
import com.ibm.cohort.measure.wrapper.element.ListEntryWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureReportGroupPopulationWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureReportGroupWrapper;
import com.ibm.cohort.measure.wrapper.element.ReferenceWrapper;
import com.ibm.cohort.measure.wrapper.resource.ListWrapper;
import com.ibm.cohort.measure.wrapper.resource.MeasureReportWrapper;
import com.ibm.cohort.measure.wrapper.resource.ObservationWrapper;

/*
 * KWAS TODO WHAT HAPPENS WHEN AN INCOMPATIBLE OBJECT IS PASSED???
 *       SHOULD I RETURN OPTIONAL<>???
 * WHAT ABOUT HAVING THE FACTORY HANDLE ALL MODELS AT ONCE?!?
 */
public interface WrapperFactory {

    <T extends BaseWrapper> T wrapResource(Object resource);

//    ResourceWrapper wrapResource(Object resource);
//
//    PatientWrapper wrapPatient(Object patient);
//
//    KnowledgeResourceWrapper wrapKnowledgeResource(Object knowledgeResource);
//
//    MeasureGroupPopulationWrapper wrapMeasureGroupPopulation(Object measureGroupPopulation);
//
//    CodingWrapper wrapCoding(Object coding);

    MeasureReportGroupWrapper newMeasureReportGroup();

    MeasureReportGroupPopulationWrapper newMeasureReportGroupPopulation();

    ListWrapper newList();

    ListEntryWrapper newListEntry();

    ReferenceWrapper newReference();

    MeasureReportWrapper newMeasureReport();

    ObservationWrapper newObservation();

    CodingWrapper newCoding();

    CodeableConceptWrapper newCodeableConcept();

    ExtensionWrapper newExtension();

}
