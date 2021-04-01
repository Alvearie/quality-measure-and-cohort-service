/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.valueset;


//taken from cqf-tooling's CodeSystemLookupDictionary.java
public class CodeSystemLookup {
	private CodeSystemLookup() {}
	
	public static  String getUrlFromName(String name) {
		switch (name) {
			case "ActCode": return "http://terminology.hl7.org/CodeSystem/v3-ActCode";
			case "ActMood": return "http://terminology.hl7.org/CodeSystem/v3-ActMood";
			case "ActPriority": return "http://terminology.hl7.org/CodeSystem/v3-ActPriority";
			case "ActReason": return "http://terminology.hl7.org/CodeSystem/v3-ActReason";
			case "ActRelationshipType": return "http://terminology.hl7.org/CodeSystem/v3-ActRelationshipType";
			case "ActStatus": return "http://terminology.hl7.org/CodeSystem/v3-ActStatus";
			case "AddressUse": return "http://terminology.hl7.org/CodeSystem/v3-AddressUse";
			case "AdministrativeGender": return "http://terminology.hl7.org/CodeSystem/v3-AdministrativeGender";
			case "AdministrativeSex": return "http://terminology.hl7.org/CodeSystem/v2-0001";
			case "CPT": return "http://www.ama-assn.org/go/cpt";
			case "CPT-CAT-II": return "http://www.ama-assn.org/go/cpt";
			case "CVX": return "http://hl7.org/fhir/sid/cvx";
			case "Confidentiality": return "http://terminology.hl7.org/CodeSystem/v3-Confidentiality";
			case "DischargeDisposition": return "http://terminology.hl7.org/CodeSystem/discharge-disposition";
			case "EntityNamePartQualifier": return "http://terminology.hl7.org/CodeSystem/v3-EntityNamePartQualifier";
			case "EntityNameUse": return "http://terminology.hl7.org/CodeSystem/v3-EntityNameUse";
			case "HCPCS": return "http://terminology.hl7.org/CodeSystem/HCPCS";
			case "HCPCS Level I: CPT": return "http://terminology.hl7.org/CodeSystem/HCPCS";
			case "HCPCS Level II": return "urn:oid:2.16.840.1.113883.6.285";
			case "HSLOC": return "http://terminology.hl7.org/CodeSystem/hsloc";
			case "ICD10": return "http://terminology.hl7.org/CodeSystem/icd10";
			case "ICD10CM": return "http://hl7.org/fhir/sid/icd-10-cm";
			case "ICD10PCS": return "http://www.cms.gov/Medicare/Coding/ICD10";
			case "ICD9": return "http://terminology.hl7.org/CodeSystem/icd9";
			case "ICD9CM": return "http://terminology.hl7.org/CodeSystem/icd9cm";
			case "ICD9PCS": return "urn:oid:2.16.840.1.113883.6.104";
			case "LOINC": return "http://loinc.org";
			case "LanguageAbilityMode": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityMode";
			case "LanguageAbilityProficiency": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityProficiency";
			case "LivingArrangement": return "http://terminology.hl7.org/CodeSystem/v3-LivingArrangement";
			case "MaritalStatus": return "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus";
			case "NCI": return "http://ncithesaurus-stage.nci.nih.gov";
			case "NDFRT": return "http://terminology.hl7.org/CodeSystem/nciVersionOfNDF-RT";
			case "NUCCPT": return "http://nucc.org/provider-taxonomy";
			case "Provider Taxonomy": return "http://nucc.org/provider-taxonomy";
			case "NullFlavor": return "http://terminology.hl7.org/CodeSystem/v3-NullFlavor";
			case "ObservationInterpretation": return "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation";
			case "ObservationValue": return "http://terminology.hl7.org/CodeSystem/v3-ObservationValue";
			case "ParticipationFunction": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationFunction";
			case "ParticipationMode": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationMode";
			case "ParticipationType": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";
			case "RXNORM": return "http://www.nlm.nih.gov/research/umls/rxnorm";
			case "ReligiousAffiliation": return "http://terminology.hl7.org/CodeSystem/v3-ReligiousAffiliation";
			case "RoleClass": return "http://terminology.hl7.org/CodeSystem/v3-RoleClass";
			case "RoleCode": return "http://terminology.hl7.org/CodeSystem/v3-RoleCode";
			case "RoleStatus": return "http://terminology.hl7.org/CodeSystem/v3-RoleStatus";
			case "SNOMEDCT": return "http://snomed.info/sct";
			case "SNOMED CT US Edition": return "http://snomed.info/sct";
			case "UBREV": return "http://terminology.hl7.org/CodeSystem/nubc-UB92";
			case "UBTOB": return "http://terminology.hl7.org/CodeSystem/nubc-UB92";
			case "POS": return "http://terminology.hl7.org/CodeSystem/POS";
			case "CDCREC": return "http://terminology.hl7.org/CodeSystem/PHRaceAndEthnicityCDC";
			case "Modifier": return "http://www.ama-assn.org/go/cpt";
			case "CDT": return "http://terminology.hl7.org/CodeSystem/CD2";
			case "mediaType": return "http://terminology.hl7.org/CodeSystem/v3-mediatypes";
			case "SOP":  return "urn:oid:2.16.840.1.113883.3.221.5";
			case "UCUM": return "http://unitsofmeasure.org";
			case "UMLS": return "http://terminology.hl7.org/CodeSystem/umls";
			default: throw new IllegalArgumentException("Unknown CodeSystem name: " + name);
		}
	}
}
