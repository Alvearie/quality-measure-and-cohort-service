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

	private static final String MEASURE_PARAMETER = "measure-parameter";
	public static final String MEASURE_PARAMETER_URL = BASE_URL + STRUCTURE_DEFINITION + MEASURE_PARAMETER;

	private static final String MEASURE_PARAMETER_VALUE = "measure-parameter-value";
	public static final String MEASURE_PARAMETER_VALUE_URL = BASE_URL + STRUCTURE_DEFINITION + MEASURE_PARAMETER_VALUE;

	private static final String PARAMETER_VALUE = "parameter-value";
	public static final String PARAMETER_VALUE_URL = BASE_URL + STRUCTURE_DEFINITION + PARAMETER_VALUE;

	private static final String PARAMETER_DEFAULT = "default-value";
	public static final String PARAMETER_DEFAULT_URL = BASE_URL + STRUCTURE_DEFINITION + PARAMETER_DEFAULT;
	
	public static final String MEASUREMENT_PERIOD = "Measurement Period";
	public static final String PRODUCT_LINE = "Product Line";
}
