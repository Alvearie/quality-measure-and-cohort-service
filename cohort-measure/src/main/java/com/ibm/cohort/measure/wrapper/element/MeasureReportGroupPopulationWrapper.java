/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface MeasureReportGroupPopulationWrapper extends ElementWrapper {

    void setCode(String code);

    void setCount(int count);

    void setSubjectResults(ReferenceWrapper referenceBuilder);

}
