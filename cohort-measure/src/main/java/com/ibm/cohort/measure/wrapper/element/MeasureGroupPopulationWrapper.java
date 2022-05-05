/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

import com.ibm.cohort.measure.wrapper.element.ElementWrapper;

public interface MeasureGroupPopulationWrapper extends ElementWrapper {

    boolean hasCriteria();

    String getExpression();

    CodeableConceptWrapper getCode();
    void setCode(CodeableConceptWrapper code);

}
