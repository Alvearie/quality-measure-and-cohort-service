/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.fhir.resolver;

/**
 * Resolves resources by various FHIR inspired methods.
 *
 * @param <T> The resource type to resolve.
 */
public interface FhirResourceResolver<T> {

    T resolveById(String id);
    T resolveByName(String name, String version);
    T resolveByCanonicalUrl(String canonicalUrl);
    T resolveByIdentifier(String value, String system, String version);

}
