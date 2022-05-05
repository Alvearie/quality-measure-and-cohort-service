/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface MeasureReportGroupPopulationWrapper extends ElementWrapper {


    CodeableConceptWrapper getCode();
    void setCode(CodeableConceptWrapper code);

    // KWAS TODO: can I use primitives???
    int getCount();
    void setCount(int count);

    void setSubjectResults(ReferenceWrapper referenceBuilder);

}
