/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.fhir.handler;

import com.ibm.cohort.cql.version.VersionHandler;

import java.util.List;

/**
 * Retrieves various FHIR related fields from the specified type.
 * Can also extract information from a specified identifier type.
 *
 * @param <T> The resource type to retrieve fields from.
 * @param <I> The identifier type found on {@link T}.
 */
public interface ResourceHandler<T, I> extends VersionHandler<T> {

    Class<T> getSupportedClass();

    @Override
    String getVersion(T resource);

    String getId(T resource);
    void setId(String id, T resource);

    String getName(T resource);
    String getUrl(T resource);

    List<I> getIdentifiers(T resource);
    String getIdentifierValue(I identifier);
    String getIdentifierSystem(I identifier);

}
