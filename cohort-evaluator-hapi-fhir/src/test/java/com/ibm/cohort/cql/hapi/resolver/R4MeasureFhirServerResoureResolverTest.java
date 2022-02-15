/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class R4MeasureFhirServerResoureResolverTest extends R4FhirServerResourceResolverBaseTest {

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
        String measureId = "Measure1-id";
        mockRequest(
                WireMock::get,
                "/Measure/" + measureId,
                200,
                IOUtils.resourceToString("/fhir/rest-test/Measure-1.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Measure> resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
        Measure measure = resolver.resolveById(measureId);
        String expected = "Measure/" + measureId;
        Assert.assertEquals(expected, measure.getId());
    }

    @Test
    public void resolveById_resourceNotFound() {
        String id = "Measure1-id";
        mockRequest(
                WireMock::get,
                "/Measure/" + id,
                404,
                ""
        );

        FhirResourceResolver<Measure> resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
        Measure measure = resolver.resolveById(id);
        Assert.assertNull(measure);
    }

    @Test
    public void resolveByName() throws IOException {
        String name = "Measure1-name";
        String version = "1.0.0";
        mockRequest(
                WireMock::get,
                "/Measure\\?name%3Aexact=" + name + "&version=" + version,
                200,
                IOUtils.resourceToString("/fhir/rest-test/MeasureBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Measure> resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
        Measure measure = resolver.resolveByName(name, version);
        String expected = "Measure/Measure1-id";
        Assert.assertEquals(expected, measure.getId());
    }

    @Test
    public void resolveByName_noVersion() throws IOException {
        String name = "Measure1-name";
        mockRequest(
                WireMock::get,
                "/Measure\\?name%3Aexact=" + name,
                200,
                IOUtils.resourceToString("/fhir/rest-test/MeasureBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Measure> resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
        Measure measure = resolver.resolveByName(name, null);
        String expected = "Measure/Measure1-id";
        Assert.assertEquals(expected, measure.getId());
    }

    @Test
    public void resolveByCanonicalUrl() throws IOException {
        String canonicalUrl = "http://fake.url.com/Measure/Measure1|1.0.0";
        mockRequest(
                WireMock::get,
                "/Measure\\?url=.*Measure.*Measure1&version=1.0.0",
                200,
                IOUtils.resourceToString("/fhir/rest-test/MeasureBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Measure> resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
        Measure measure = resolver.resolveByCanonicalUrl(canonicalUrl);
        String expected = "Measure/Measure1-id";
        Assert.assertEquals(expected, measure.getId());
    }

    @Test
    public void resolveByCanonicalUrl_noVersion() throws IOException {
        String canonicalUrl = "http://fake.url.com/Measure/Measure1";
        mockRequest(
                WireMock::get,
                "/Measure\\?url=.*Measure.*Measure1",
                200,
                IOUtils.resourceToString("/fhir/rest-test/MeasureBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Measure> resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
        Measure measure = resolver.resolveByCanonicalUrl(canonicalUrl);
        String expected = "Measure/Measure1-id";
        Assert.assertEquals(expected, measure.getId());
    }

    @Test
    public void resolveByIdentifier() throws IOException {
        String idValue = "Measure1-idValue";
        String idSystem = "Measure1-idSystem";
        String version = "1.0.0";
        mockRequest(
                WireMock::get,
                "/Measure\\?identifier=" + idSystem + "%7C" + idValue + "&version=" + version,
                200,
                IOUtils.resourceToString("/fhir/rest-test/MeasureBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Measure> resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
        Measure measure = resolver.resolveByIdentifier(idValue, idSystem, version);
        String expected = "Measure/Measure1-id";
        Assert.assertEquals(expected, measure.getId());
    }

    @Test
    public void resolveByIdentifier_noVersion() throws IOException {
        String idValue = "Measure1-idValue";
        String idSystem = "Measure1-idSystem";
        mockRequest(
                WireMock::get,
                "/Measure\\?identifier=" + idSystem + "%7C" + idValue,
                200,
                IOUtils.resourceToString("/fhir/rest-test/MeasureBundle.json", StandardCharsets.UTF_8)
        );

        FhirResourceResolver<Measure> resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
        Measure measure = resolver.resolveByIdentifier(idValue, idSystem, null);
        String expected = "Measure/Measure1-id";
        Assert.assertEquals(expected, measure.getId());
    }

}
