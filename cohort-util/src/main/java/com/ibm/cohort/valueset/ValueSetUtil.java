/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
//todo this possibly wants it's own module, since this will only be used by two modules? idk.
package com.ibm.cohort.valueset;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class ValueSetUtil {

	public static void validateArtifact(ValueSetArtifact valueSetArtifact){
			if(valueSetArtifact.getUrl() == null){
				//todo new kind of exception? Invalid Format? something specific
				throw new IllegalArgumentException("URL must be supplied");
			}
			if(valueSetArtifact.getFhirResource() == null){
				throw new IllegalArgumentException("Fhir Resource must be supplied");
			}
			if(valueSetArtifact.getFhirResource().getId() == null || valueSetArtifact.getFhirResource().getId().equals("")){
				throw new IllegalArgumentException("Identifier must be supplied, ensure that either the OID or the ID field is filled in");
			}
			if(valueSetArtifact.getFhirResource().getVersion() == null || valueSetArtifact.getFhirResource().getVersion().equals("")){
				throw new IllegalArgumentException("Value Set Version must be supplied");
			}
			if(valueSetArtifact.getFhirResource().getCompose().getInclude() == null || valueSetArtifact.getFhirResource().getCompose().getInclude().size() == 0){
				throw new IllegalArgumentException("Value set must include codes but no codes were included.");
			}
	}

	public static ValueSetArtifact createArtifact(InputStream is) throws IOException {
		XSSFSheet informationSheet;
		try (XSSFWorkbook wb = new XSSFWorkbook(is)) {
			informationSheet = wb.getSheetAt(wb.getSheetIndex("Expansion List"));
		}
		catch (IllegalArgumentException e){
			throw new RuntimeException("Spreadsheet is missing required sheet \"Expansion List\"", e);
		}
		ValueSet valueSet = new ValueSet();
		boolean inCodesSection = false;
		valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
		HashMap<String, List<ValueSet.ConceptReferenceComponent>> codeSystemToCodes = new HashMap<>();
		String url = "http://cts.nlm.nih.gov/fhir/ValueSet/";
		String identifier = null;
		for (Row currentRow : informationSheet) {
			String code = currentRow.getCell(0) == null ? "" : currentRow.getCell(0).getStringCellValue();
			if (!code.equals("") && currentRow.getCell(1) != null && !inCodesSection ) {
				String value;
				try{
					value = currentRow.getCell(1).getStringCellValue();
				}
				catch (IllegalArgumentException e){
					throw new RuntimeException("Missing data must be supplied", e);
				}
				switch (currentRow.getCell(0).getStringCellValue().toLowerCase()) {
					case "value set name":
						valueSet.setName(value);
						valueSet.setTitle(value);
						break;
					case "id":
						valueSet.setId(value);
						identifier = value;
						break;
					case "oid":
						if(valueSet.getId() == null) {
							valueSet.setId(value);
							identifier = value;
						}
						break;
					case "url":
						url = value.endsWith("/") ? value : value + "/";
					case "definition version":
						valueSet.setVersion(value);
						break;
					case "code":
						inCodesSection = true;
					default:
						break;
				}
			}
			else if (inCodesSection) {
				String display;
				try {
					 display = currentRow.getCell(1).getStringCellValue();
				}
				catch (Exception e){
					throw new RuntimeException("Codes must be supplied when uploading Value Sets", e);
				}
				String codeSystem = CodeSystemLookup.getUrlFromName(currentRow.getCell(2).getStringCellValue());
				ValueSet.ConceptReferenceComponent concept = new ValueSet.ConceptReferenceComponent();
				concept.setCode(code);
				concept.setDisplay(display);

				List<ValueSet.ConceptReferenceComponent> conceptsSoFar
						= codeSystemToCodes.computeIfAbsent(codeSystem, x -> new ArrayList<>());

				conceptsSoFar.add(concept);
			}

		}
		if(identifier == null || identifier.equals("")){
			throw new RuntimeException("There must be an Identifier specified! Please populate the ID field");
		}
		valueSet.setUrl(url + identifier);
		valueSet.setId(identifier);
		ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();

		for (Map.Entry<String, List<ValueSet.ConceptReferenceComponent>> singleInclude : codeSystemToCodes.entrySet()) {
			ValueSet.ConceptSetComponent component = new ValueSet.ConceptSetComponent();
			component.setSystem(singleInclude.getKey());
			component.setConcept(singleInclude.getValue());
			compose.addInclude(component);
		}
		valueSet.setCompose(compose);
		ValueSetArtifact artifact = new ValueSetArtifact();
		artifact.setName(valueSet.getName());
		artifact.setFhirResource(valueSet);
		artifact.setUrl(valueSet.getUrl());
		return artifact;
	}

	public static void importArtifacts(IGenericClient client, List<ValueSetArtifact> valueSetArtifacts, boolean updateIfExists) {
		for(ValueSetArtifact valueSetArtifact : valueSetArtifacts) {
			importArtifact(client, valueSetArtifact, updateIfExists);
		}
	}

	public static void deleteValueSet(IGenericClient client, String url){
		client.delete().resourceConditionalByType("ValueSet").where(ValueSet.URL.matches().value(url)).execute();
	}

	public static String importArtifact(IGenericClient client, ValueSetArtifact valueSetArtifact, boolean updateIfExists) {

		Bundle bundle = client.search().forResource(ValueSet.class).where(ValueSet.URL.matches().value(valueSetArtifact.getUrl()))
				.returnBundle(Bundle.class).execute();
		MethodOutcome outcome;
		if(bundle.getEntry().size() > 0){
			String[] url = bundle.getEntry().get(0).getFullUrl().split("/");
			valueSetArtifact.getFhirResource().setId(url[url.length-1]);
			if(updateIfExists){
				outcome = client.update().resource(client.getFhirContext().newJsonParser().encodeResourceToString(valueSetArtifact.getFhirResource()))
						.conditional().where(ValueSet.URL.matches().value(valueSetArtifact.getUrl())).execute();
			}
			else{
				return null;
			}
		}
		else {
			outcome = client.create().resource(client.getFhirContext().newJsonParser().encodeResourceToString(valueSetArtifact.getFhirResource())).execute();
		}
		valueSetArtifact.setId(outcome.getId().getIdPart());

		return valueSetArtifact.getId();

	}
}
