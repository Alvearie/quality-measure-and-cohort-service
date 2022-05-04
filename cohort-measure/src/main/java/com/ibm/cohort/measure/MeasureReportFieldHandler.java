/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure;

public interface MeasureReportFieldHandler<T> {

    MeasureReportType getSubjectListType();
    MeasureReportType getIndividualType();

}
