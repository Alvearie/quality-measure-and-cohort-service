/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cdm;

public class CDMConstants {
	
	private CDMConstants() {}
	
	private static final String STRUCTURE_DEFINITION = "/StructureDefinition/";
	
	public static final String BASE_URL = "http://ibm.com/fhir/cdm";
	
	public static final String CARE_GAP = "care-gap";
	public static final String CDM_CODE_SYSTEM_MEASURE_POPULATION_TYPE = BASE_URL + "/CodeSystem/measure-population-type";
	
	public static final String EVIDENCE = "measure-report-evidence";
	public static final String EVIDENCE_URL = BASE_URL + STRUCTURE_DEFINITION + EVIDENCE;
	
	public static final String EVIDENCE_TEXT = "measure-report-evidence-text";
	public static final String EVIDENCE_TEXT_URL = BASE_URL + STRUCTURE_DEFINITION + EVIDENCE_TEXT;
	
	public static final String EVIDENCE_VALUE = "measure-report-evidence-value";
	public static final String EVIDENCE_VALUE_URL = BASE_URL + STRUCTURE_DEFINITION + EVIDENCE_VALUE;
	
	
}
