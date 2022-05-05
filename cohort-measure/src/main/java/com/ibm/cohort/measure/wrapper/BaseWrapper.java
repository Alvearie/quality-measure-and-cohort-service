/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper;

public interface BaseWrapper {

    Object getNativeObject();

    String fhirType();

}
