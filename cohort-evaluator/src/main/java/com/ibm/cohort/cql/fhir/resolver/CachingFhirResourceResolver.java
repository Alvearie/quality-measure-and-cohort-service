/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.fhir.resolver;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A {@link FhirResourceResolver} that delegates to a provided {@link FhirResourceResolver}
 * while caching all returned values in a set of {@link WeakHashMap}s to speed up
 * future repeat requests.
 *
 * <p> Resources resolved via one method will not be cached for other resolution methods.
 *
 * @param <T> The resource type to resolve.
 */
public class CachingFhirResourceResolver<T> implements FhirResourceResolver<T> {

    private final Map<String, T> nameCache = new WeakHashMap<>();
    private final Map<String, T> nameVersionCache = new WeakHashMap<>();
    private final Map<String, T> canonicalUrlCache = new WeakHashMap<>();
    private final Map<String, T> identifierCache = new WeakHashMap<>();

    private final FhirResourceResolver<T> resolver;

    public CachingFhirResourceResolver(FhirResourceResolver<T> resolver) {
        this.resolver = resolver;
    }

    @Override
    public T resolveById(String id) {
        return nameCache.computeIfAbsent(id, resolver::resolveById);
    }

    @Override
    public T resolveByName(String name, String version) {
        String key = name + "-" + version;
        return nameVersionCache.computeIfAbsent(key, x -> resolver.resolveByName(name, version));
    }

    @Override
    public T resolveByCanonicalUrl(String canonicalUrl) {
        return canonicalUrlCache.computeIfAbsent(canonicalUrl, resolver::resolveByCanonicalUrl);
    }

    @Override
    public T resolveByIdentifier(String value, String system, String version) {
        String key = value + "-" + system + "-" + version;
        return identifierCache.computeIfAbsent(key, x -> resolver.resolveByIdentifier(value, system, version));
    }

}
