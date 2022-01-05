/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.impl.BaseClient;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValueSetUtilTest {

	private static int HTTP_PORT = 0;

	@BeforeClass
	public static void setUpBeforeClass() {
		// get a random local port for test use
		try (ServerSocket socket = new ServerSocket(0)) {
			HTTP_PORT = socket.getLocalPort();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(
			WireMockConfiguration.options().port(HTTP_PORT)
	);

	@Test
	public void testUnsuppliedUrl(){
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("URL must be supplied");
		ValueSetArtifact artifact = new ValueSetArtifact();
		ValueSetUtil.validateArtifact(artifact);
	}

	@Test
	public void testUnsuppliedIFhirResource(){
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Fhir Resource must be supplied");
		ValueSetArtifact artifact = new ValueSetArtifact();
		artifact.setUrl("fakeUrl");
		ValueSetUtil.validateArtifact(artifact);
	}

	@Test
	public void testUnsuppliedId(){
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Identifier must be supplied, ensure that either the OID or the ID field is filled in");
		ValueSetArtifact artifact = new ValueSetArtifact();
		artifact.setUrl("fakeUrl");
		artifact.setFhirResource(new ValueSet());
		ValueSetUtil.validateArtifact(artifact);
	}

	@Test
	public void testUnsuppliedVersion(){
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Value Set Version must be supplied");
		ValueSetArtifact artifact = new ValueSetArtifact();
		artifact.setUrl("fakeUrl");
		ValueSet fakeValueSet = new ValueSet();
		fakeValueSet.setId("fakeId");
		artifact.setFhirResource(fakeValueSet);
		ValueSetUtil.validateArtifact(artifact);
	}

	@Test
	public void testUnsuppliedCodes(){
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Value set must include codes but no codes were included.");
		ValueSetArtifact artifact = new ValueSetArtifact();
		artifact.setUrl("fakeUrl");
		ValueSet fakeValueSet = new ValueSet();
		fakeValueSet.setId("fakeId");
		fakeValueSet.setVersion("fakeVersion");
		artifact.setFhirResource(fakeValueSet);
		ValueSetUtil.validateArtifact(artifact);
	}

	@Test
	public void testCorrectValidation(){
		ValueSetArtifact artifact = new ValueSetArtifact();
		artifact.setUrl("fakeUrl");
		ValueSet fakeValueSet = new ValueSet();
		fakeValueSet.setId("fakeId");
		fakeValueSet.setVersion("fakeVersion");
		ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
		ValueSet.ConceptSetComponent component = new ValueSet.ConceptSetComponent();
		component.setConcept(Collections.singletonList(new ValueSet.ConceptReferenceComponent(new CodeType("fakeCode"))));
		compose.setInclude(Collections.singletonList(component));
		fakeValueSet.setCompose(compose);
		artifact.setFhirResource(fakeValueSet);
		ValueSetUtil.validateArtifact(artifact);
	}

	@Test
	public void testArtifactCreation() throws IOException {
		String valueSetInput = "src/test/resources/2.16.840.1.113762.1.4.1114.7.xlsx";
		File tempFile = new File(valueSetInput);
		byte[] byteArrayInput = Files.readAllBytes(Paths.get(tempFile.getAbsolutePath()));
		ValueSetArtifact artifact = ValueSetUtil.createArtifact(new ByteArrayInputStream(byteArrayInput), null);
		assertEquals("testValueSet", artifact.getFhirResource().getId());
		assertEquals("http://cts.nlm.nih.gov/fhir/ValueSet/testValueSet", artifact.getUrl());
		assertEquals("Value Set For Testing Uploads", artifact.getName());
	}

	@Test
	public void testMapCreation() throws IOException {
		String codeSystemInput = "src/test/resources/codeSystemOverride.txt";
		Map<String, String> expectedMap = new HashMap<>();
		expectedMap.put("tomato", "http://abc.com");
		expectedMap.put("excedrin", "http://nih.no.com");
		Map<String, String> codeSystemMap = ValueSetUtil.getMapFromInputStream(new FileInputStream(new File(codeSystemInput)));
		assertEquals(expectedMap.size(), codeSystemMap.size());
		assertEquals(expectedMap.entrySet(), codeSystemMap.entrySet());
	}

	@Test
	public void testImportArtifact_createNew() throws IOException {
		String valueSetInput = "src/test/resources/2.16.840.1.113762.1.4.1114.7.xlsx";
		File tempFile = new File(valueSetInput);
		byte[] byteArrayInput = Files.readAllBytes(Paths.get(tempFile.getAbsolutePath()));
		ValueSetArtifact artifact = ValueSetUtil.createArtifact(new ByteArrayInputStream(byteArrayInput), null);

		FhirContext context = FhirContext.forR4();
		IParser parser = context.newJsonParser();

		mockGetCall("/metadata", 200, parser.encodeResourceToString(getCapabilityStatement()));
		mockGetCall("/ValueSet\\?url=[^&]+", 200, parser.encodeResourceToString(getValueSetBundle()));

		String expected = "new-valueset";
		mockPostCall("/ValueSet", 201, expected);

		IGenericClient client = context.newRestfulGenericClient("http://localhost:" + HTTP_PORT);

		String actual = ValueSetUtil.importArtifact(client, artifact, false);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testImportArtifact_updateExisting() throws IOException {
		String valueSetInput = "src/test/resources/2.16.840.1.113762.1.4.1114.7.xlsx";
		File tempFile = new File(valueSetInput);
		byte[] byteArrayInput = Files.readAllBytes(Paths.get(tempFile.getAbsolutePath()));
		ValueSetArtifact artifact = ValueSetUtil.createArtifact(new ByteArrayInputStream(byteArrayInput), null);

		FhirContext context = FhirContext.forR4();
		IParser parser = context.newJsonParser();

		mockGetCall("/metadata", 200, parser.encodeResourceToString(getCapabilityStatement()));

		String expected = "2.16.840.1.113762.1.4.1114.7";
		ValueSet valueSet = getValueSet("http://some/url/" + expected);
		Bundle bundle = getValueSetBundle(valueSet);
		mockGetCall("/ValueSet\\?url=[^&]+", 200, parser.encodeResourceToString(bundle));
		mockPutCall("/ValueSet\\?url=[^&]+", 200, expected);

		IGenericClient client = context.newRestfulGenericClient("http://localhost:" + HTTP_PORT);

		String actual = ValueSetUtil.importArtifact(client, artifact, true);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testImportArtifact_updateExisting_updateBlocked() throws IOException {
		String valueSetInput = "src/test/resources/2.16.840.1.113762.1.4.1114.7.xlsx";
		File tempFile = new File(valueSetInput);
		byte[] byteArrayInput = Files.readAllBytes(Paths.get(tempFile.getAbsolutePath()));
		ValueSetArtifact artifact = ValueSetUtil.createArtifact(new ByteArrayInputStream(byteArrayInput), null);

		FhirContext context = FhirContext.forR4();
		IParser parser = context.newJsonParser();

		mockGetCall("/metadata", 200, parser.encodeResourceToString(getCapabilityStatement()));

		String expected = "2.16.840.1.113762.1.4.1114.7";
		ValueSet valueSet = getValueSet("http://some/url/" + expected);
		Bundle bundle = getValueSetBundle(valueSet);
		mockGetCall("/ValueSet\\?url=[^&]+", 200, parser.encodeResourceToString(bundle));

		IGenericClient client = context.newRestfulGenericClient("http://localhost:" + HTTP_PORT);

		String actual = ValueSetUtil.importArtifact(client, artifact, false);
		Assert.assertNull(actual);
	}

	private void mockGetCall(String urlRegex, int code, String responseBody) {
		ResponseDefinitionBuilder response = WireMock.aResponse()
				.withStatus(code)
				.withHeader("Content-Type", "application/json")
				.withBody(responseBody);
		MappingBuilder builder = WireMock.get(WireMock.urlMatching(urlRegex))
				.willReturn(response);
		WireMock.stubFor(builder);
	}

	private void mockPostCall(String urlRegex, int code, String newId) {
		ResponseDefinitionBuilder response = WireMock.aResponse()
				.withStatus(code)
				.withHeader("Location", newId);
		MappingBuilder builder = WireMock.post(WireMock.urlMatching(urlRegex))
				.willReturn(response);
		WireMock.stubFor(builder);
	}

	private void mockPutCall(String urlRegex, int code, String newId) {
		ResponseDefinitionBuilder response = WireMock.aResponse()
				.withStatus(code)
				.withHeader("Location", newId);
		MappingBuilder builder = WireMock.put(WireMock.urlMatching(urlRegex))
				.willReturn(response);
		WireMock.stubFor(builder);
	}

	private ValueSet getValueSet(String url) {
		ValueSet retVal = new ValueSet();

		retVal.setUrl(url);

		return retVal;
	}

	private Bundle getValueSetBundle(ValueSet... valueSets) {
		Bundle retVal = new Bundle();

		List<BundleEntryComponent> components = Arrays.stream(valueSets)
				.map(x -> new BundleEntryComponent().setResource(x).setFullUrl(x.getUrl()))
				.collect(Collectors.toList());
		retVal.setEntry(components);

		return retVal;
	}

	private CapabilityStatement getCapabilityStatement() {
		CapabilityStatement metadata = new CapabilityStatement();
		metadata.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
		return metadata;
	}

}