/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Before;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureHelperTest extends BaseMeasureTest {
	
	private static final String DEFAULT_VERSION = "1.0.0";
	
	private MeasureResolutionProvider<Measure> provider;

	@Before
	public void setUp() {
		FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
		FhirClientBuilder builder = factory.newFhirClientBuilder(fhirContext);
		IGenericClient client = builder.createFhirClient(getFhirServerConfig());
		provider = new RestFhirMeasureResolutionProvider(client);
		
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
	}
	
	@Test
	public void testResolveByID() throws Exception {
		Measure measure = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/test.xml"), "Female");
		mockFhirResourceRetrieval(measure);
		
		Measure actual = MeasureHelper.loadMeasure(measure.getId(), provider);
		assertNotNull(actual);
	}
	
	@Test
	public void testResolveByCanonicalUrl() throws Exception {
		Measure measure = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/test.xml"), "Female");
		measure.setUrl("http://fhir.ibm.com/fhir-server/api/v4/Measure/Test-1.0.0");
		
		MappingBuilder builder = get(urlMatching("/Measure\\?url=.*"));
		mockFhirResourceRetrieval(builder,getBundle(measure));
		
		Measure actual = MeasureHelper.loadMeasure(measure.getUrl(), provider);
		assertNotNull(actual);
	}

	@Test
	public void testResolveByIdentifier() throws Exception {
		String identifierSystem = "system1";
		String identifierValue = "val1";

		Measure measure = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/test.xml"), "Female");

		Identifier identifier = new Identifier();
		identifier.setValue(identifierValue);
		identifier.setSystem(identifierSystem);

		measure.setIdentifier(Collections.singletonList(identifier));
		measure.setVersion("1.0.0");

		MappingBuilder builder = get(urlPathEqualTo("/Measure"))
				.withQueryParam("identifier", new EqualToPattern(identifierSystem + '|' + identifierValue));
		mockFhirResourceRetrieval(builder,getBundle(measure));

		Measure actual = MeasureHelper.loadMeasure(identifier, "", provider);
		assertNotNull(actual);
	}

	@Test
	public void testResolveByIdentifier_getLatestMeasure() throws Exception {
		String identifierSystem = "system1";
		String identifierValue = "val1";

		Measure measure1 = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/test.xml"), "Female");
		Measure measure2 = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/test.xml"), "Female");
		Measure measure3 = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/test.xml"), "Female");

		measure1.setVersion("1.0.0");
		measure2.setVersion("2.0.0");

		Identifier identifier = new Identifier();
		identifier.setValue(identifierValue);
		identifier.setSystem(identifierSystem);

		measure1.setIdentifier(Collections.singletonList(identifier));
		measure2.setIdentifier(Collections.singletonList(identifier));
		measure3.setIdentifier(Collections.singletonList(identifier));

		MappingBuilder builder = get(urlPathEqualTo("/Measure"))
				.withQueryParam("identifier", new EqualToPattern(identifierSystem + '|' + identifierValue));
		mockFhirResourceRetrieval(builder,getBundle(measure1, measure2, measure3));

		Measure actual = MeasureHelper.loadMeasure(identifier, null, provider);
		assertNotNull(actual);
	}

	@Test
	public void testResolveByIdentifierAndVersion() throws Exception {
		String identifierSystem = "system1";
		String identifierValue = "val1";
		String measureVersion = "4.5.6";

		Measure measure = getCohortMeasure("Test", getLibrary("123", DEFAULT_VERSION, "cql/basic/test.xml"), "Female");

		Identifier identifier = new Identifier();
		identifier.setValue(identifierValue);
		identifier.setSystem(identifierSystem);

		measure.setIdentifier(Collections.singletonList(identifier));

		measure.setVersion(measureVersion);

		MappingBuilder builder = get(urlPathEqualTo("/Measure"))
				.withQueryParam("identifier", new EqualToPattern(identifierSystem + '|' + identifierValue))
				.withQueryParam("version", new EqualToPattern(measureVersion));
		mockFhirResourceRetrieval(builder,getBundle(measure));

		Measure actual = MeasureHelper.loadMeasure(identifier, measureVersion, provider);
		assertNotNull(actual);
	}
}
