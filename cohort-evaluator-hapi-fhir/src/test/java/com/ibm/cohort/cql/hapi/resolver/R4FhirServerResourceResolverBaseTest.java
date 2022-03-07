/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Before;
import org.junit.Rule;

import java.net.ServerSocket;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class R4FhirServerResourceResolverBaseTest {

    protected static int getValidHttpPort() {
        // get a random local port for test use
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            options().port(getCurrentHttpPort())
                    .notifier(new ConsoleNotifier(true))
    );

    protected abstract int getCurrentHttpPort();

    protected IParser parser;
    protected IGenericClient client;

    @Before
    public void setup() {
        if (client == null) {
            FhirContext context = FhirContext.forR4();
            parser = context.newJsonParser();
            client = context.newRestfulGenericClient("http://localhost:" + getCurrentHttpPort());
        }

        mockMetdataRequest();
    }

    protected void mockRequest(
            Function<UrlPattern, MappingBuilder> method,
            String urlRegex,
            int status,
            String responseBody
    ) {
        ResponseDefinitionBuilder responseBuilder = WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(status)
                .withBody(responseBody);
        UrlPattern urlPattern = WireMock.urlMatching(urlRegex);
        MappingBuilder mappingBuilder = method.apply(urlPattern)
                .willReturn(responseBuilder);
        WireMock.stubFor(mappingBuilder);
    }

    private void mockMetdataRequest() {
        CapabilityStatement metadata = new CapabilityStatement();
        metadata.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        mockRequest(
                WireMock::get,
                "/metadata",
                200,
                parser.encodeResourceToString(metadata)
        );
    }

}
