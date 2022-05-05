/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

import java.util.List;

public interface MeasureReportGroupWrapper extends ElementWrapper {

    // KWAS TODO: Do we need a quantity builder or wrapper??
    void setScoreAsQuantity(double value);

    List<MeasureReportGroupPopulationWrapper> getPopulation();
    void addPopulation(MeasureReportGroupPopulationWrapper measureReportGroupPopulation);

}
