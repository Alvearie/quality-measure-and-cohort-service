/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertNotNull;

import org.hl7.fhir.r4.model.Measure;
import org.junit.Before;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.ibm.cohort.engine.FhirClientFactory;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureHelperTest extends BaseMeasureTest {
	
	private MeasureResolutionProvider<Measure> provider;

	@Before
	public void setUp() {
		IGenericClient client = FhirClientFactory.newInstance(fhirContext).createFhirClient(getFhirServerConfig());
		provider = new RestFhirMeasureResolutionProvider(client);
		
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
	}
	
	@Test
	public void testResolveByID() throws Exception {
		Measure measure = getCohortMeasure("Test", getLibrary("123", "cql/basic/test.xml"), "Female");
		mockFhirResourceRetrieval(measure);
		
		Measure actual = MeasureHelper.loadMeasure(measure.getId(), provider);
		assertNotNull(actual);
	}
	
	@Test
	public void testResolveByCanonicalUrl() throws Exception {
		Measure measure = getCohortMeasure("Test", getLibrary("123", "cql/basic/test.xml"), "Female");
		measure.setUrl("http://fhir.ibm.com/fhir-server/api/v4/Measure/Test-1.0.0");
		
		MappingBuilder builder = get(urlMatching("/Measure\\?url=.*"));
		mockFhirResourceRetrieval(builder,getBundle(measure));
		
		Measure actual = MeasureHelper.loadMeasure(measure.getUrl(), provider);
		assertNotNull(actual);
	}
}
