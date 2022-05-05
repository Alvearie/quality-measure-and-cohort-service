/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.resource;

import com.ibm.cohort.measure.wrapper.element.MeasureGroupWrapper;
import com.ibm.cohort.measure.wrapper.element.MeasureSupplementalDataWrapper;
import com.ibm.cohort.measure.wrapper.element.ReferenceWrapper;

import java.util.List;

public interface MeasureWrapper extends CanonicalResourceWrapper {

    String getScoringCode();

    List<MeasureGroupWrapper> getGroup();

    List<MeasureSupplementalDataWrapper> getSupplementalData();

}
