/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.fhir.resolver;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CachingFhirResourceResolverTest {

    @Test
    public void resolveById() {
        String id = "id";
        Object expected = new Object();

        FhirResourceResolver<Object> mockResolver = Mockito.mock(FhirResourceResolver.class);
        Mockito.when(mockResolver.resolveById(id))
                .thenReturn(expected);
        FhirResourceResolver<Object> resolver = new CachingFhirResourceResolver<>(mockResolver);

        for (int i = 0; i < 50; i++) {
            Object actual = resolver.resolveById(id);
            Assert.assertEquals(expected, actual);
        }
        Mockito.verify(mockResolver, Mockito.times(1)).resolveById(id);
    }

    @Test
    public void resolveByName() {
        String name = "name";
        String version = "1.0.0";
        Object expected = new Object();

        FhirResourceResolver<Object> mockResolver = Mockito.mock(FhirResourceResolver.class);
        Mockito.when(mockResolver.resolveByName(name, version))
                .thenReturn(expected);
        FhirResourceResolver<Object> resolver = new CachingFhirResourceResolver<>(mockResolver);

        for (int i = 0; i < 50; i++) {
            Object actual = resolver.resolveByName(name, version);
            Assert.assertEquals(expected, actual);
        }
        Mockito.verify(mockResolver, Mockito.times(1)).resolveByName(name, version);
    }

    @Test
    public void resolveByCanonicalUrl() {
        String url = "url";
        Object expected = new Object();

        FhirResourceResolver<Object> mockResolver = Mockito.mock(FhirResourceResolver.class);
        Mockito.when(mockResolver.resolveByCanonicalUrl(url))
                .thenReturn(expected);
        FhirResourceResolver<Object> resolver = new CachingFhirResourceResolver<>(mockResolver);

        for (int i = 0; i < 50; i++) {
            Object actual = resolver.resolveByCanonicalUrl(url);
            Assert.assertEquals(expected, actual);
        }
        Mockito.verify(mockResolver, Mockito.times(1)).resolveByCanonicalUrl(url);
    }

    @Test
    public void resolveByIdentifier() {
        String value = "value";
        String system = "system";
        String version = "1.0.0";
        Object expected = new Object();

        FhirResourceResolver<Object> mockResolver = Mockito.mock(FhirResourceResolver.class);
        Mockito.when(mockResolver.resolveByIdentifier(value, system, version))
                .thenReturn(expected);
        FhirResourceResolver<Object> resolver = new CachingFhirResourceResolver<>(mockResolver);

        for (int i = 0; i < 50; i++) {
            Object actual = resolver.resolveByIdentifier(value, system, version);
            Assert.assertEquals(expected, actual);
        }
        Mockito.verify(mockResolver, Mockito.times(1)).resolveByIdentifier(value, system, version);
    }

}
