/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface MeasureSupplementalDataWrapper extends ElementWrapper {

    // KWAS TODO: Delete if we don't need.

    // getCriteria().getExpression()
    String getExpression();

    // getCode().getText()
    String getCode();

}
