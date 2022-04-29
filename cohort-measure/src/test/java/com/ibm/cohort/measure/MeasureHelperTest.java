/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.resolver.R4FhirServerResourceResolverFactory;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Before;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.ibm.cohort.engine.r4.builder.IdentifierBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureHelperTest extends MeasureTestBase {
	
	private static final String DEFAULT_VERSION = "1.0.0";

	private FhirResourceResolver<Measure> resolver;

	@Before
	public void setUp() {
		FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
		FhirClientBuilder builder = factory.newFhirClientBuilder(fhirContext);
		IGenericClient client = builder.createFhirClient(getFhirServerConfig());
		resolver = R4FhirServerResourceResolverFactory.createMeasureResolver(client);
		
		mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());
	}
	
	@Test
	public void testResolveByID() throws Exception {
		Measure measure = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/Test-1.0.0.xml"), "Female");
		mockFhirResourceRetrieval(measure);
		
		Measure actual = MeasureHelper.loadMeasure(measure.getId(), resolver);
		assertNotNull(actual);
		assertEquals("Test", actual.getName());
	}
	
	@Test
	public void testResolveByCanonicalUrl() throws Exception {
		Measure measure = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/Test-1.0.0.xml"), "Female");
		measure.setUrl("http://fhir.ibm.com/fhir-server/api/v4/Measure/Test-1.0.0");
		
		MappingBuilder builder = WireMock.get(WireMock.urlMatching("/Measure\\?url=.*&_format=json"));
		mockFhirResourceRetrieval(builder,getBundle(measure));
		
		Measure actual = MeasureHelper.loadMeasure(measure.getUrl(), resolver);
		assertNotNull(actual);
		assertEquals("Test", actual.getName());
	}

	@Test
	public void testResolveByIdentifier() throws Exception {
		String identifierSystem = "system1";
		String identifierValue = "val1";

		Measure measure = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/Test-1.0.0.xml"), "Female");

		org.hl7.fhir.r4.model.Identifier identifier = new IdentifierBuilder()
				.buildValue(identifierValue)
				.buildSystem(identifierSystem)
				.build();

		measure.setIdentifier(Collections.singletonList(identifier));
		measure.setVersion("1.0.0");

		MappingBuilder builder = WireMock.get(WireMock.urlPathEqualTo("/Measure"))
				.withQueryParam("identifier", new EqualToPattern(identifierSystem + '|' + identifierValue))
				.withQueryParam("_format", new EqualToPattern("json"));
		mockFhirResourceRetrieval(builder,getBundle(measure));

		Measure actual = MeasureHelper.loadMeasure(new Identifier(identifierSystem, identifierValue), "", resolver);
		assertNotNull(actual);
		assertEquals("Test", actual.getName());
	}

	@Test
	public void testResolveByIdentifier_getLatestMeasure() throws Exception {
		String identifierSystem = "system1";
		String identifierValue = "val1";

		Measure measure1 = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/Test-1.0.0.xml"), "Female");
		Measure measure2 = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/Test-1.0.0.xml"), "Female");
		Measure measure3 = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/Test-1.0.0.xml"), "Female");

		measure1.setVersion("1.0.0");
		measure2.setVersion("2.0.0");

		org.hl7.fhir.r4.model.Identifier identifier = new IdentifierBuilder()
				.buildValue(identifierValue)
				.buildSystem(identifierSystem)
				.build();

		measure1.setIdentifier(Collections.singletonList(identifier));
		measure2.setIdentifier(Collections.singletonList(identifier));
		measure3.setIdentifier(Collections.singletonList(identifier));

		MappingBuilder builder = WireMock.get(WireMock.urlPathEqualTo("/Measure"))
				.withQueryParam("identifier", new EqualToPattern(identifierSystem + '|' + identifierValue))
				.withQueryParam("_format", new EqualToPattern("json"));
		mockFhirResourceRetrieval(builder,getBundle(measure1, measure2, measure3));

		Measure actual = MeasureHelper.loadMeasure(new Identifier(identifierSystem, identifierValue), null, resolver);
		assertNotNull(actual);
		assertEquals("Test", actual.getName());
		assertEquals("2.0.0", actual.getVersion());
	}

	@Test
	public void testResolveByIdentifierAndVersion() throws Exception {
		String identifierSystem = "system1";
		String identifierValue = "val1";
		String measureVersion = "4.5.6";

		Measure measure = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/Test-1.0.0.xml"), "Female");

		org.hl7.fhir.r4.model.Identifier identifier = new IdentifierBuilder()
				.buildValue(identifierValue)
				.buildSystem(identifierSystem)
				.build();

		measure.setIdentifier(Collections.singletonList(identifier));

		measure.setVersion(measureVersion);

		MappingBuilder builder = WireMock.get(WireMock.urlPathEqualTo("/Measure"))
				.withQueryParam("identifier", new EqualToPattern(identifierSystem + '|' + identifierValue))
				.withQueryParam("version", new EqualToPattern(measureVersion))
				.withQueryParam("_format", new EqualToPattern("json"));
		mockFhirResourceRetrieval(builder,getBundle(measure));

		Measure actual = MeasureHelper.loadMeasure(new Identifier(identifierSystem, identifierValue), measureVersion, resolver);
		assertNotNull(actual);
		assertEquals("Test", actual.getName());
		assertEquals(measureVersion, actual.getVersion());
	}
}
