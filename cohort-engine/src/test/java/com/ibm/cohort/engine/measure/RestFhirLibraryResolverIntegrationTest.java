/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.ibm.cohort.cql.fhir.resolver.CachingFhirResourceResolver;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.resolver.R4FhirServerResourceResolverFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class RestFhirLibraryResolverIntegrationTest extends BaseFhirTest {

    private static final String TEST_URL = "http://somewhere.com/cds/Test|1.0.0";
    private static final String DEFAULT_VERSION = "1.0.0";
    private FhirResourceResolver<Library> resolver;

    @Before
    public void setUp() {
        FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
        FhirClientBuilder builder = factory.newFhirClientBuilder(fhirContext);
        IGenericClient client = builder.createFhirClient(getFhirServerConfig());

        resolver = R4FhirServerResourceResolverFactory.createLibraryResolver(client);
    }

    @Test
    public void resolveLibraryById___returns_library_when_found() throws Exception {
        mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

        Library library = getLibrary("Test", DEFAULT_VERSION, "cql/basic/Test-1.0.0.cql");
        mockFhirResourceRetrieval( library );

        Library actual = resolver.resolveById(library.getId());
        assertNotNull(actual);
        assertEquals(actual.getName(), library.getName());
    }

    @Test
    public void resolveLibraryById___null_when_not_found() {
        mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

        MappingBuilder builder = get(urlMatching("/Library.+"));
        setAuthenticationParameters(getFhirServerConfig(), builder);
        stubFor(builder.willReturn(aResponse().withStatus(404)));

        Library actual = resolver.resolveById("Test");
        assertNull(actual);
    }

    @Test
    public void resolveLibraryById_twice___returns_cached_data() throws Exception {
        resolver = new CachingFhirResourceResolver<>(resolver);

        mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

        Library library = getLibrary("Test", DEFAULT_VERSION, "cql/basic/Test-1.0.0.cql");
        mockFhirResourceRetrieval( library );

        Library actual = resolver.resolveById(library.getId());
        assertNotNull(actual);

        actual = resolver.resolveById(library.getId());
        assertNotNull(actual);

        verify(1, getRequestedFor(urlMatching("/Library.*")));
    }

    @Test
    public void resolveLibraryByName___returns_library_when_found() throws Exception {
        mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

        Library library = getLibrary("Test", DEFAULT_VERSION, "cql/basic/Test-1.0.0.cql");
        library.setVersion("1.0.0");
        MappingBuilder mapping = get(urlMatching("/Library\\?name%3Aexact=Test&version=1.0.0.*"));
        mockFhirResourceRetrieval( mapping, makeBundle(library) );

        Library actual = resolver.resolveByName(library.getName(), library.getVersion());
        assertNotNull(actual);
        assertEquals(actual.getName(), library.getName());
    }

    @Test
    public void resolveLibraryByName___null_when_not_found() {
        mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

        MappingBuilder mapping = get(urlMatching("/Library\\?name%3Aexact=Test&version=1.0.0.*"));
        mockFhirResourceRetrieval(mapping, makeBundle());

        Library actual = resolver.resolveByName("Test", "1.0.0");
        assertNull(actual);
    }

    @Test
    public void resolveLibraryByName_twice___returns_cached_data() throws Exception {
        resolver = new CachingFhirResourceResolver<>(resolver);

        mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

        Library library = getLibrary("Test", DEFAULT_VERSION, "cql/basic/Test-1.0.0.cql");
        library.setVersion("1.0.0");
        MappingBuilder mapping = get(urlMatching("/Library\\?name%3Aexact=Test&version=1.0.0.*"));
        mockFhirResourceRetrieval( mapping, makeBundle(library) );

        Library actual = resolver.resolveByName(library.getName(), library.getVersion());
        assertNotNull(actual);

        actual = resolver.resolveByName(library.getName(), library.getVersion());
        assertNotNull(actual);

        verify(1, getRequestedFor(urlMatching("/Library.*")));
    }

    @Test
    public void resolveLibraryByCanonicalUrl___returns_library_when_found() throws Exception {
        Library library = getLibrary("Test", DEFAULT_VERSION, "cql/basic/Test-1.0.0.cql");
        library.setUrl(TEST_URL);

        Library actual = runTest(TEST_URL, makeBundle( library ));
        assertNotNull(actual);
        assertEquals(library.getUrl(), actual.getUrl());
    }

    @Test
    public void resolveLibraryByCanonicalUrl_twice___returns_cached_data() throws Exception {
        resolver = new CachingFhirResourceResolver<>(resolver);

        Library library = getLibrary("Test", DEFAULT_VERSION, "cql/basic/Test-1.0.0.cql");
        library.setUrl(TEST_URL);

        Library actual = runTest(TEST_URL, makeBundle( library ));
        assertNotNull(actual);
        assertEquals(library.getUrl(), actual.getUrl());

        resolver.resolveByCanonicalUrl(TEST_URL);
        verify(1, getRequestedFor(urlMatching("/Library\\?url=.*")));
    }

    @Test
    public void resolveLibraryByCanonicalUrl__null_when_library_not_found() {
        Library actual = runTest(TEST_URL, makeBundle());
        Assert.assertNull(actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveLibraryByCanonicalUrl___exception_when_multiple_library_found() throws Exception {
        Library library1 = getLibrary("Test", DEFAULT_VERSION, "cql/basic/Test-1.0.0.cql");
        library1.setUrl(TEST_URL);

        Library library2 = getLibrary("Test", DEFAULT_VERSION, "cql/basic/Test-1.0.0.cql");
        library2.setUrl(TEST_URL);

        runTest(TEST_URL, makeBundle(library1, library2));
    }

    protected Library runTest(String url, Bundle bundle) {
        mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

        MappingBuilder builder = get(urlMatching("/Library\\?url=.*"));
        mockFhirResourceRetrieval(builder, bundle);

        return resolver.resolveByCanonicalUrl(url);
    }
}
