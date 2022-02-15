/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.provider;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

import java.io.InputStream;

/**
 * An implementation of {@link CqlLibrarySourceProvider} that queries the provided
 * {@link LibrarySourceProvider} instance.
 */
public class SourceProviderBasedCqlLibrarySourceProvider implements CqlLibrarySourceProvider {

    private final LibrarySourceProvider backingProvider;

    public SourceProviderBasedCqlLibrarySourceProvider(LibrarySourceProvider backingProvider) {
        this.backingProvider = backingProvider;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier versionedIdentifier) {
        return backingProvider.getLibrarySource(versionedIdentifier);
    }

}
