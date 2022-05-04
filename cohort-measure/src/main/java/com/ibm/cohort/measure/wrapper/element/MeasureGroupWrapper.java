/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

import java.util.List;

public interface MeasureGroupWrapper extends ElementWrapper {

    List<MeasureGroupPopulationWrapper> getPopulation();
    void addPopulation(MeasureGroupPopulationWrapper measureGroupPopulationWrapper);

}
