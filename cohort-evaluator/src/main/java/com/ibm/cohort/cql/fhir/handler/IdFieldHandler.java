/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.fhir.handler;

public interface IdFieldHandler<T> {

    String getId(T resource);
    void setId(String id, T resource);

}
