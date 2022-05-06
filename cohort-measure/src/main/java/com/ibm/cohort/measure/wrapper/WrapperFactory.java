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
import com.ibm.cohort.measure.wrapper.element.ParameterDefinitionWrapper;
import com.ibm.cohort.measure.wrapper.element.PeriodWrapper;
import com.ibm.cohort.measure.wrapper.element.RatioWrapper;
import com.ibm.cohort.measure.wrapper.element.ReferenceWrapper;
import com.ibm.cohort.measure.wrapper.resource.ListWrapper;
import com.ibm.cohort.measure.wrapper.resource.MeasureReportWrapper;
import com.ibm.cohort.measure.wrapper.resource.ObservationWrapper;
import com.ibm.cohort.measure.wrapper.type.BooleanWrapper;
import com.ibm.cohort.measure.wrapper.type.CanonicalWrapper;
import com.ibm.cohort.measure.wrapper.type.DateTimeWrapper;
import com.ibm.cohort.measure.wrapper.type.DateWrapper;
import com.ibm.cohort.measure.wrapper.type.DecimalWrapper;
import com.ibm.cohort.measure.wrapper.type.IntegerWrapper;
import com.ibm.cohort.measure.wrapper.element.QuantityWrapper;
import com.ibm.cohort.measure.wrapper.element.RangeWrapper;
import com.ibm.cohort.measure.wrapper.type.StringWrapper;
import com.ibm.cohort.measure.wrapper.type.TimeWrapper;

/*
 * KWAS TODO WHAT HAPPENS WHEN AN INCOMPATIBLE OBJECT IS PASSED???
 *       SHOULD I RETURN OPTIONAL<>???
 * WHAT ABOUT HAVING THE FACTORY HANDLE ALL MODELS AT ONCE?!?
 */
public interface WrapperFactory {

    <T extends BaseWrapper> T wrapObject(Object resource);

    // Resources
    ListWrapper newList();

    MeasureReportWrapper newMeasureReport();

    ObservationWrapper newObservation();

    // Elements
    MeasureReportGroupWrapper newMeasureReportGroup();

    MeasureReportGroupPopulationWrapper newMeasureReportGroupPopulation();

    ListEntryWrapper newListEntry();

    ReferenceWrapper newReference();

    CodingWrapper newCoding();

    CodeableConceptWrapper newCodeableConcept();

    ExtensionWrapper newExtension();

    ParameterDefinitionWrapper newParameterDefinition();

    PeriodWrapper newPeriod();

    QuantityWrapper newQuantity();

    RangeWrapper newRange();

    RatioWrapper newRatio();

    // Types
    CanonicalWrapper newCanonical();

    DecimalWrapper newDecimal();

    TimeWrapper newTime();

    BooleanWrapper newBoolean();

    DateTimeWrapper newDateTime();

    StringWrapper newString();

    IntegerWrapper newInteger();

    DateWrapper newDate();

}
