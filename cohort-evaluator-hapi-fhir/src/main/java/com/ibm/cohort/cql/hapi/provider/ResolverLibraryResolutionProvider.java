/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.provider;

import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;

/**
 * A {@link LibraryResolutionProvider} that delegates to a provided
 * {@link FhirResourceResolver} instance.
 *
 * @param <T> The resource type to provide.
 */
public class ResolverLibraryResolutionProvider<T> implements LibraryResolutionProvider<T> {

    private final FhirResourceResolver<T> resourceResolver;

    public ResolverLibraryResolutionProvider(FhirResourceResolver<T> resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    @Override
    public T resolveLibraryById(String libraryId) {
        return resourceResolver.resolveById(libraryId);
    }

    @Override
    public T resolveLibraryByName(String libraryName, String libraryVersion) {
        return resourceResolver.resolveByName(libraryName, libraryVersion);
    }

    @Override
    public T resolveLibraryByCanonicalUrl(String libraryUrl) {
        return resourceResolver.resolveByCanonicalUrl(libraryUrl);
    }

    @Override
    public void update(T library) {
        throw new UnsupportedOperationException("Library updates are not supported");
    }
}
