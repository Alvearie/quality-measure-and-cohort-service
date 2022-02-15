/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Library;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class R4LibraryFhirServerResourceResolverTest extends R4FhirServerResourceResolverBaseTest {

    private static int HTTP_PORT = 0;
    @BeforeClass
    public static void setUpBeforeClass() {
        HTTP_PORT = R4FhirServerResourceResolverBaseTest.getValidHttpPort();
    }

    @Override
    protected int getCurrentHttpPort() {
        return HTTP_PORT;
    }

    @Test
    public void resolveById() throws IOException {
        String libraryId = "Library1-id";
        mockRequest(
                WireMock::get,
                "/Library/" + libraryId,
                200,
                IOUtils.resourceToString("/fhir/rest-test/Library-1.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Library> resolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(client);
        Library library = resolver.resolveById(libraryId);
        String expected = "Library/" + libraryId;
        Assert.assertEquals(expected, library.getId());
    }

    @Test
    public void resolveById_resourceNotFound() {
        String id = "Library1-id";
        mockRequest(
                WireMock::get,
                "/Library/" + id,
                404,
                ""
        );

        FhirResourceResolver<Library> resolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(client);
        Library library = resolver.resolveById(id);
        Assert.assertNull(library);
    }

    @Test
    public void resolveByName() throws IOException {
        String name = "Library1-name";
        String version = "1.0.0";
        mockRequest(
                WireMock::get,
                "/Library\\?name%3Aexact=" + name + "&version=" + version,
                200,
                IOUtils.resourceToString("/fhir/rest-test/LibraryBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Library> resolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(client);
        Library library = resolver.resolveByName(name, version);
        String expected = "Library/Library1-id";
        Assert.assertEquals(expected, library.getId());
    }

    @Test
    public void resolveByName_noVersion() throws IOException {
        String name = "Library1-name";
        mockRequest(
                WireMock::get,
                "/Library\\?name%3Aexact=" + name,
                200,
                IOUtils.resourceToString("/fhir/rest-test/LibraryBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Library> resolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(client);
        Library library = resolver.resolveByName(name, null);
        String expected = "Library/Library1-id";
        Assert.assertEquals(expected, library.getId());
    }

    @Test
    public void resolveByCanonicalUrl() throws IOException {
        String canonicalUrl = "http://fake.url.com/Library/Library1|1.0.0";
        mockRequest(
                WireMock::get,
                "/Library\\?url=.*Library.*Library1&version=1.0.0",
                200,
                IOUtils.resourceToString("/fhir/rest-test/LibraryBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Library> resolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(client);
        Library library = resolver.resolveByCanonicalUrl(canonicalUrl);
        String expected = "Library/Library1-id";
        Assert.assertEquals(expected, library.getId());
    }

    @Test
    public void resolveByCanonicalUrl_noVersion() throws IOException {
        String canonicalUrl = "http://fake.url.com/Library/Library1";
        mockRequest(
                WireMock::get,
                "/Library\\?url=.*Library.*Library1",
                200,
                IOUtils.resourceToString("/fhir/rest-test/LibraryBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Library> resolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(client);
        Library library = resolver.resolveByCanonicalUrl(canonicalUrl);
        String expected = "Library/Library1-id";
        Assert.assertEquals(expected, library.getId());
    }

    @Test
    public void resolveByIdentifier() throws IOException {
        String idValue = "Library1-idValue";
        String idSystem = "Library1-idSystem";
        String version = "1.0.0";
        mockRequest(
                WireMock::get,
                "/Library\\?identifier=" + idSystem + "%7C" + idValue + "&version=" + version,
                200,
                IOUtils.resourceToString("/fhir/rest-test/LibraryBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Library> resolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(client);
        Library library = resolver.resolveByIdentifier(idValue, idSystem, version);
        String expected = "Library/Library1-id";
        Assert.assertEquals(expected, library.getId());
    }

    @Test
    public void resolveByIdentifier_noVersion() throws IOException {
        String idValue = "Library1-idValue";
        String idSystem = "Library1-idSystem";
        mockRequest(
                WireMock::get,
                "/Library\\?identifier=" + idSystem + "%7C" + idValue,
                200,
                IOUtils.resourceToString("/fhir/rest-test/LibraryBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Library> resolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(client);
        Library library = resolver.resolveByIdentifier(idValue, idSystem, null);
        String expected = "Library/Library1-id";
        Assert.assertEquals(expected, library.getId());
    }

}
