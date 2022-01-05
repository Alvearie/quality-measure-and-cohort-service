/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.provider;

import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ResolverLibraryResolutionProviderTest {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String URL = "url";

    private ResolverLibraryResolutionProvider<Object> provider;
    private final Object expected = new Object();

    @Before
    public void setUp() {
        FhirResourceResolver<Object> resolver = Mockito.mock(FhirResourceResolver.class);

        Mockito.when(resolver.resolveById(ID))
                .thenReturn(expected);
        Mockito.when(resolver.resolveByName(NAME, VERSION))
                .thenReturn(expected);
        Mockito.when(resolver.resolveByCanonicalUrl(URL))
                .thenReturn(expected);

        provider = new ResolverLibraryResolutionProvider<>(resolver);
    }

    @Test
    public void resolveLibraryById_pass() {
        Assert.assertSame(expected, provider.resolveLibraryById(ID));
    }

    @Test
    public void resolveLibraryById_fail() {
        Assert.assertNull(provider.resolveLibraryById("bad"));
    }

    @Test
    public void resolveLibraryByName_pass() {
        Assert.assertSame(expected, provider.resolveLibraryByName(NAME, VERSION));
    }

    @Test
    public void resolveLibraryByName_fail() {
        Assert.assertNull(provider.resolveLibraryByName("bad", "bad"));
    }

    @Test
    public void resolveLibraryByUrl_pass() {
        Assert.assertSame(expected, provider.resolveLibraryByCanonicalUrl(URL));
    }

    @Test
    public void resolveLibraryByUrl_fail() {
        Assert.assertNull(provider.resolveLibraryByCanonicalUrl("bad"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void update_throwsException() {
        provider.update(expected);
    }
}
