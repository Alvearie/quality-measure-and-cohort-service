/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.provider;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;

public class SourceProviderBasedCqlLibrarySourceProviderTest {

    @Test
    public void getLibrarySource() {
        InputStream expected = Mockito.mock(InputStream.class);
        VersionedIdentifier identifier = new VersionedIdentifier();
        LibrarySourceProvider backingProvider = Mockito.mock(LibrarySourceProvider.class);
        Mockito.when(backingProvider.getLibrarySource(identifier))
                .thenReturn(expected);
        SourceProviderBasedCqlLibrarySourceProvider provider = new SourceProviderBasedCqlLibrarySourceProvider(backingProvider);
        Assert.assertEquals(expected, provider.getLibrarySource(identifier));
    }

}
