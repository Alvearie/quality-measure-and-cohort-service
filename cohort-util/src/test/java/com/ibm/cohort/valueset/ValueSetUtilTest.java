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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class ValueSetUtilTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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

}