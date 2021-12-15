/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URLEncoder;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations.ResourceType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class RestFhirMeasureResolutionProviderTest  extends BaseFhirTest {
	MeasureResolutionProvider<Measure> provider;
	
	@Before
	public void setUp() {
		FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
		FhirClientBuilder builder = factory.newFhirClientBuilder(fhirContext);
		IGenericClient client = builder.createFhirClient(getFhirServerConfig());
		provider = new RestFhirMeasureResolutionProvider(client);
		
		mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());
	}
	
	@Test
	public void resource_not_found_by_id___returns_null() {
		mockNotFound("/Measure/non-existent-id\\?_format=json");
		assertNull( provider.resolveMeasureById("non-existent-id") );
	}
	
	@Test
	public void resource_not_found_by_url___returns_null() {
		mockEmptySearchResults(ResourceType.MEASURE.toCode());
		assertNull( provider.resolveMeasureByCanonicalUrl("http://nowhere.com/Measure/nothing|1.0.0") );
	}
	
	@Test
	public void resource_not_found_by_name_with_version___returns_null() {
		mockEmptySearchResults(ResourceType.MEASURE.toCode());
		assertNull( provider.resolveMeasureByName("NotAMeasure", "1.0.0") );
	}
	
	@Test
	public void resource_not_found_by_name_without_version___returns_null() {
		mockEmptySearchResults(ResourceType.MEASURE.toCode());
		assertNull( provider.resolveMeasureByName("NotAMeasure", null) );
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void too_many_resources_found_by_name_with_version___throws_exception()  throws Exception {
		mockMultipleSearchResults("Measure1", "1.0.0", "1.0.0");
		provider.resolveMeasureByName("Measure1", "1.0.0");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void too_many_resources_found_by_url_with_version___throws_exception() throws Exception {
		String measureName = "Measure1";
		mockMultipleSearchResults(measureName, "1.0.0", "1.0.0");
		provider.resolveMeasureByCanonicalUrl(getUrlForName(measureName, "1.0.0"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void too_many_resources_found_by_identifier_with_version___throws_exception() throws Exception {
		String measureName = "Measure1";
		Identifier identifier = mockMultipleSearchResults(measureName, "1.0.0", "1.0.0");
		provider.resolveMeasureByIdentifier(identifier, "1.0.0");
	}
	
	@Test
	public void resolve_by_name_without_version___returns_newest_semver() throws Exception {
		String measureName = "Measure1";
		mockMultipleSearchResults(measureName, "1.0.0", "1.1.0");
		Measure resolved = provider.resolveMeasureByName( measureName, null );
		assertEquals("1.1.0", resolved.getVersion());
	}
	
	@Test
	public void resolve_by_url_without_version___returns_newest_semver() throws Exception {
		String measureName = "Measure1";
		mockMultipleSearchResults(measureName, "1.0.0", "1.1.0");
		Measure resolved = provider.resolveMeasureByCanonicalUrl( getUrlForName(measureName) );
		assertEquals("1.1.0", resolved.getVersion());
	}
	
	@Test
	public void resolve_by_identifier_without_version___returns_newest_semver() throws Exception {
		String measureName = "Measure1";
		Identifier identifier = mockMultipleSearchResults(measureName, "2.0.0", "1.1.0");
		Measure resolved = provider.resolveMeasureByIdentifier(identifier, null);
		assertEquals("2.0.0", resolved.getVersion());
	}
	
	protected Identifier mockMultipleSearchResults(String measureName, String version1, String version2) throws Exception {
		Identifier identifier = new Identifier().setSystem("http://alvearie.io/health/Measure/id").setValue(measureName);
		mockMultipleSearchResults( measureName, version1, version2, identifier );
		return identifier;
	}
	
	protected void mockMultipleSearchResults(String measureName, String version1, String version2, Identifier identifier) throws Exception {
		
		Measure measure1v1 = new Measure();
		measure1v1.setId("measure1-1.0.0");
		measure1v1.setName(measureName);
		measure1v1.setUrl(getUrlForName(measureName));
		measure1v1.setVersion(version1);
		measure1v1.addIdentifier(identifier);
		mockFhirResourceRetrieval("/Measure/" + measure1v1.getId(), measure1v1);
		
		Measure measure1v2 = new Measure();
		measure1v2.setId("measure2-1.1.0");
		measure1v2.setName(measure1v1.getName());
		measure1v2.setUrl(measure1v1.getUrl());
		measure1v2.setVersion(version2);
		measure1v2.addIdentifier(identifier);
		mockFhirResourceRetrieval("/Measure/" + measure1v2.getId(), measure1v2);

		Bundle bundle = new Bundle();
		bundle.addEntry(new Bundle.BundleEntryComponent().setResource(measure1v1));
		bundle.addEntry(new Bundle.BundleEntryComponent().setResource(measure1v2));
		mockFhirResourceRetrieval("/Measure?name%3Aexact=" + measure1v1.getName() + "&_format=json", bundle);
		mockFhirResourceRetrieval("/Measure?name%3Aexact=" + measure1v1.getName() + "&version=" + measure1v1.getVersion() + "&_format=json", bundle);
		mockFhirResourceRetrieval("/Measure?url=" + URLEncoder.encode(measure1v1.getUrl(), "UTF-8") + "&_format=json", bundle);
		mockFhirResourceRetrieval("/Measure?url=" + URLEncoder.encode(measure1v1.getUrl(), "UTF-8") + "&version=" + measure1v1.getVersion() + "&_format=json", bundle);
		mockFhirResourceRetrieval("/Measure?identifier=" + URLEncoder.encode(identifier.getSystem() + "|" + identifier.getValue(), "UTF-8") + "&_format=json", bundle);
		mockFhirResourceRetrieval("/Measure?identifier=" + URLEncoder.encode(identifier.getSystem() + "|" + identifier.getValue(), "UTF-8") + "&version=" + measure1v1.getVersion() + "&_format=json", bundle);
	}
	
	public String getUrlForName(String measureName) {
		return "http://ibm.com/health/Measure/" + measureName;
	}
	
	public String getUrlForName(String measureName, String version) {
		return getUrlForName(measureName) + "|" + version;
	}
}
