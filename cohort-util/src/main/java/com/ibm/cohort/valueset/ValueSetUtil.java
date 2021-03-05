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

	public static ValueSetArtifact createArtifact(InputStream is) throws IOException {
		XSSFSheet informationSheet;
		try (XSSFWorkbook wb = new XSSFWorkbook(is)) {
			informationSheet = wb.getSheetAt(wb.getSheetIndex("Expansion List"));
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
				String value = currentRow.getCell(1).getStringCellValue();
				switch (currentRow.getCell(0).getStringCellValue().toLowerCase()) {
					case "valueset set name":
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
				String display = currentRow.getCell(1).getStringCellValue();
				String codeSystem = currentRow.getCell(2).getStringCellValue();
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
		artifact.setResource(valueSet);
		artifact.setUrl(valueSet.getUrl());
		return artifact;
	}

	public static void importArtifacts(IGenericClient client, List<ValueSetArtifact> valueSetArtifacts) {
		importArtifacts(client, valueSetArtifacts, false);
	}

	public static void importArtifacts(IGenericClient client, List<ValueSetArtifact> valueSetArtifacts, boolean continueIFExists) {

		for (ValueSetArtifact valueSetArtifact : valueSetArtifacts) {
			Bundle bundle = client.search().forResource(ValueSet.class).where(ValueSet.URL.matches().value(valueSetArtifact.getUrl()))
					.returnBundle(Bundle.class).execute();
			if (bundle.getEntry().size() > 0) {
				valueSetArtifact.setId(bundle.getEntryFirstRep().getResource().getIdElement().getIdPart());
				if (continueIFExists)  {
					MethodOutcome outcome = client.update().resource(client.getFhirContext().newJsonParser().encodeResourceToString(valueSetArtifact.getResource())).execute();
					if (outcome.getCreated()) {
						valueSetArtifact.setId(outcome.getId().getIdPart());
					}
				}
			} else {
				MethodOutcome outcome = client.create().resource(client.getFhirContext().newJsonParser().encodeResourceToString(valueSetArtifact.getResource())).execute();
				if (outcome.getCreated()) {
					valueSetArtifact.setId(outcome.getId().getIdPart());
				}
			}
		}
	}
}
