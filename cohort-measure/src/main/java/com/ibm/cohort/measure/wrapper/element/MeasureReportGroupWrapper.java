/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface MeasureReportGroupWrapper extends ElementWrapper {

    // KWAS TODO: Do we need a quantity builder or wrapper??
    void setScoreAsQuantity(double value);

    void addPopulation(MeasureReportGroupPopulationWrapper measureReportGroupPopulation);

}
