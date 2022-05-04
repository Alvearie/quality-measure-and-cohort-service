/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.fhir.handler;

import com.ibm.cohort.cql.version.VersionFieldHandler;

import java.util.List;

/**
 * Handles various FHIR related fields from the specified type.
 * Can also handle information from a specified identifier type.
 *
 * @param <T> The resource type to handle fields from.
 * @param <I> The identifier type found on {@link T}.
 */
public interface ResourceFieldHandler<T, I> extends VersionFieldHandler<T>, IdFieldHandler<T> {

    Class<T> getSupportedClass();

    @Override
    String getVersion(T resource);

    String getName(T resource);
    String getUrl(T resource);

    List<I> getIdentifiers(T resource);
    String getIdentifierValue(I identifier);
    String getIdentifierSystem(I identifier);

}
