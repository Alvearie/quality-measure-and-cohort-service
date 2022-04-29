/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.cqfruler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.cohort.cql.hapi.FhirTestBase;
import org.cqframework.cql.elm.execution.ExpressionDef;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;
import org.mockito.Mockito;
import org.opencds.cqf.cql.engine.runtime.Code;

public class MeasureSupplementalDataEvaluationTest extends FhirTestBase {
	private static final String MALE_CODE = "M";
	private static final String FEMALE_CODE = "F";
	private static final String WHITE_CODE = "2106-3";
	private static final String ASIAN_CODE = "2028-9";
	
	private static final String SDE_RACE = "sde-race";
	
	@Test
	public void testPopulateSex() {
		Map<String, Map<String, Integer>> sdeAccumulators = getSexSDEAccumulators();
        
		assertEquals(1, sdeAccumulators.size());
		assertTrue(sdeAccumulators.containsKey(MeasureSupplementalDataEvaluation.SDE_SEX));
		
		Map<String, Integer> sexMap = sdeAccumulators.get(MeasureSupplementalDataEvaluation.SDE_SEX);
		assertEquals(1, sexMap.size());
		assertEquals(1, sexMap.get(MALE_CODE).intValue());
	}
	
	@Test
	public void testPopulateRace_multiple() {
		// Code behavior grabs first item in list and ignores the rest
		List<Coding> races = new ArrayList<>();
		races.add(getWhiteCoding());
		races.add(getAsianCoding());
		
		Map<String, Map<String, Integer>> sdeAccumulators = getSDEAccumulators(SDE_RACE, null, races, new HashMap<>());
        
		assertEquals(1, sdeAccumulators.size());
		assertTrue(sdeAccumulators.containsKey(SDE_RACE));
		
		Map<String, Integer> raceMap = sdeAccumulators.get(SDE_RACE);
		assertEquals(1, raceMap.size());
		assertEquals(1, raceMap.get(WHITE_CODE).intValue());
	}
	
	@Test
	public void testPopulateSex_notCqfConformant_null() {
		// According to http://hl7.org/fhir/us/cqfmeasures/2019May/measure-conformance.html, define names for SDE stuff should be in the format of "SDE XXX".  But the code needs a define name with a "-" in it (or the text).
		Map<String, Map<String, Integer>> sdeAccumulators = getSDEAccumulators(MeasureSupplementalDataEvaluation.SDE_SEX, null);
        
		assertEquals(1, sdeAccumulators.size());
		assertTrue(sdeAccumulators.containsKey(MeasureSupplementalDataEvaluation.SDE_SEX));
		
		Map<String, Integer> sexMap = sdeAccumulators.get(MeasureSupplementalDataEvaluation.SDE_SEX);
		assertEquals(1, sexMap.size());
		assertEquals(1, sexMap.get(MALE_CODE).intValue());
	}
	
	@Test
	public void testPopulateSex_notCqfConformant_blank() {
		// According to http://hl7.org/fhir/us/cqfmeasures/2019May/measure-conformance.html, define names for SDE stuff should be in the format of "SDE XXX".  But the code needs a define name with a "-" in it (or the text).
		Map<String, Map<String, Integer>> sdeAccumulators = getSDEAccumulators(MeasureSupplementalDataEvaluation.SDE_SEX, "");
        
		assertEquals(1, sdeAccumulators.size());
		assertTrue(sdeAccumulators.containsKey(MeasureSupplementalDataEvaluation.SDE_SEX));
		
		Map<String, Integer> sexMap = sdeAccumulators.get(MeasureSupplementalDataEvaluation.SDE_SEX);
		assertEquals(1, sexMap.size());
		assertEquals(1, sexMap.get(MALE_CODE).intValue());
	}
	
	@Test
	public void testPopulateSex_badCode() {
		Map<String, Map<String, Integer>> sdeAccumulators = getSexSDEAccumulatorsWithCode(null);
        
		assertTrue(sdeAccumulators.isEmpty());
	}
	
	@Test
	public void testPopulateSex_badCodeString() {
		Code badCode = getMaleCode();
		badCode.setCode(null);
		
		Map<String, Map<String, Integer>> sdeAccumulators = getSexSDEAccumulatorsWithCode(badCode);
        
		assertTrue(sdeAccumulators.isEmpty());
	}
	
	@Test
	public void testPopulateSex_multipleMales() {
		Map<String, Map<String, Integer>> initialAccumulators = new HashMap<>();
		Map<String, Integer> initialMale = new HashMap<>();
		initialMale.put(MALE_CODE, 1);
		initialAccumulators.put(MeasureSupplementalDataEvaluation.SDE_SEX, initialMale);
		
		Map<String, Map<String, Integer>> sdeAccumulators = getSexSDEAccumulatorsWithInitialAccumulators(initialAccumulators);
        
		assertEquals(1, sdeAccumulators.size());
		assertTrue(sdeAccumulators.containsKey(MeasureSupplementalDataEvaluation.SDE_SEX));
		
		Map<String, Integer> sexMap = sdeAccumulators.get(MeasureSupplementalDataEvaluation.SDE_SEX);
		assertEquals(1, sexMap.size());
		assertEquals(2, sexMap.get(MALE_CODE).intValue());
	}
	
	@Test
	public void testPopulateSex_mixedPatients() {
		Map<String, Map<String, Integer>> initialAccumulators = new HashMap<>();
		Map<String, Integer> initialFemale = new HashMap<>();
		initialFemale.put(FEMALE_CODE, 1);
		initialAccumulators.put(MeasureSupplementalDataEvaluation.SDE_SEX, initialFemale);
		
		Map<String, Map<String, Integer>> sdeAccumulators = getSexSDEAccumulatorsWithInitialAccumulators(initialAccumulators);
        
		assertEquals(1, sdeAccumulators.size());
		assertTrue(sdeAccumulators.containsKey(MeasureSupplementalDataEvaluation.SDE_SEX));
		
		Map<String, Integer> sexMap = sdeAccumulators.get(MeasureSupplementalDataEvaluation.SDE_SEX);
		assertEquals(2, sexMap.size());
		assertEquals(1, sexMap.get(MALE_CODE).intValue());
		assertEquals(1, sexMap.get(FEMALE_CODE).intValue());
	}
	
	@Test
	public void testProcessAccumulators() {
		Map<String, Map<String, Integer>> sdeAccumulators = getSexSDEAccumulators();
        
		MeasureReport report = new MeasureReport();
		
		MeasureSupplementalDataEvaluation.processAccumulators(report, sdeAccumulators, true, new ArrayList<>());
		
		assertNotNull(report);
		
		// EvaluatedResource should contain a reference to an observation record created for supplemental data
		assertEquals(1, report.getEvaluatedResource().size());
		
		// The observation record mentioned previously should exist within the contained resources of the measure report
		assertEquals(1, report.getContained().size());
		assertTrue(report.getContained().get(0) instanceof Observation);
		
		Observation obs = (Observation) report.getContained().get(0);
		
		// For a single patient, the code of the observation should be the supplemental data text
		assertEquals(MeasureSupplementalDataEvaluation.SDE_SEX, obs.getCode().getText());
		
		// For a single patient, the value of the observation should be the result of the appropriate define
		assertTrue(obs.getValue() instanceof CodeableConcept);
		assertEquals(MALE_CODE, ((CodeableConcept)obs.getValue()).getCoding().get(0).getCode());
		
		// Within the observation, there should be 1 extension, with two further nested extensions
		Extension obsExt = obs.getExtensionByUrl(MeasureSupplementalDataEvaluation.CQF_MEASUREINFO_URL);
		assertNotNull(obsExt);
		assertEquals(2, obsExt.getExtension().size());
		
		Extension measureNestedExt = obsExt.getExtensionByUrl(MeasureSupplementalDataEvaluation.MEASURE);
		assertTrue(measureNestedExt.getValue() instanceof CanonicalType);
		assertEquals(MeasureSupplementalDataEvaluation.CQFMEASURES_URL + report.getMeasure(), ((CanonicalType)measureNestedExt.getValue()).asStringValue());
		
		Extension populationNestedExt = obsExt.getExtensionByUrl(MeasureSupplementalDataEvaluation.POPULATION_ID);
		assertEquals(MeasureSupplementalDataEvaluation.SDE_SEX, ((StringType)populationNestedExt.getValue()).asStringValue());
	}
	
	@Test
	public void testProcessAccumulators_multiplePatients() {
		Map<String, Map<String, Integer>> initialAccumulators = new HashMap<>();
		Map<String, Integer> initialMale = new HashMap<>();
		initialMale.put(MALE_CODE, 1);
		initialAccumulators.put(MeasureSupplementalDataEvaluation.SDE_SEX, initialMale);
		
		Map<String, Map<String, Integer>> sdeAccumulators = getSexSDEAccumulatorsWithInitialAccumulators(initialAccumulators);
        
		MeasureReport report = new MeasureReport();
		
		MeasureSupplementalDataEvaluation.processAccumulators(report, sdeAccumulators, false, new ArrayList<>());
		
		assertNotNull(report);
		
		// EvaluatedResource should contain a reference to an observation record created for supplemental data
		assertEquals(1, report.getEvaluatedResource().size());
		
		// The observation record mentioned previously should exist within the contained resources of the measure report
		assertEquals(1, report.getContained().size());
		assertTrue(report.getContained().get(0) instanceof Observation);
		
		Observation obs = (Observation) report.getContained().get(0);
		
		// For a multiple patients, the code of the observation should be the supplemental data text
		assertEquals(MALE_CODE, obs.getCode().getCoding().get(0).getCode());
		
		// For a multiple patients, the value of the observation should be the result of the appropriate define
		assertTrue(obs.getValue() instanceof IntegerType);
		assertEquals("2", ((IntegerType)obs.getValue()).getValueAsString());
		
		// Within the observation, there should be 1 extension, with two further nested extensions
		Extension obsExt = obs.getExtensionByUrl(MeasureSupplementalDataEvaluation.CQF_MEASUREINFO_URL);
		assertNotNull(obsExt);
		assertEquals(2, obsExt.getExtension().size());
		
		Extension measureNestedExt = obsExt.getExtensionByUrl(MeasureSupplementalDataEvaluation.MEASURE);
		assertTrue(measureNestedExt.getValue() instanceof CanonicalType);
		assertEquals(MeasureSupplementalDataEvaluation.CQFMEASURES_URL + report.getMeasure(), ((CanonicalType)measureNestedExt.getValue()).asStringValue());
		
		Extension populationNestedExt = obsExt.getExtensionByUrl(MeasureSupplementalDataEvaluation.POPULATION_ID);
		assertEquals(MeasureSupplementalDataEvaluation.SDE_SEX, ((StringType)populationNestedExt.getValue()).asStringValue());
	}
	
	@Test
	public void testProcessAccumulators_notSDESex() {
		/* 
		 * Jill - I don't like how this part of the code works.  The code grabs everything after (and including) the "-", 
		 * and then looks for that to exist as an extension on the Patient resource.  This works for race and ethnicity 
		 * on the US-core patient profile, but since we already calculated this in the populate SDE accumulator method
		 * the only reason it's "recalculating" is because the system and display aren't on the passed in map.
		 * Plus, it's coded assuming there is a list of extensions within the extension (which is how US-Core handles race and eth) 
		 * and it magically grabs the first one... so if you have multiple this doesn't match all of them.
		*/
		
		Code white = new Code();
		white.setCode(WHITE_CODE);
		
		Map<String, Map<String, Integer>> sdeAccumulators = getSDEAccumulators(SDE_RACE, null, white, new HashMap<>());
        
		MeasureReport report = new MeasureReport();
		
		Patient mockPatient = mockPatient();
		
		Extension raceExtension = new Extension();
		// This can be anything as long as it includes "-race"
		raceExtension.setUrl("something-race");
		
		// This example was stolen from https://www.hl7.org/fhir/us/core/Patient-example.xml.html
		Extension valueExtension = new Extension();
		valueExtension.setUrl("ombCategory");

		valueExtension.setValue(getWhiteCoding());
		raceExtension.setExtension(Arrays.asList(valueExtension));
		
		Mockito.when(mockPatient.getExtension()).thenReturn(Arrays.asList(raceExtension));
		
		
		MeasureSupplementalDataEvaluation.processAccumulators(report, sdeAccumulators, true, Arrays.asList(mockPatient));
		
		assertNotNull(report);
		
		// EvaluatedResource should contain a reference to an observation record created for supplemental data
		assertEquals(1, report.getEvaluatedResource().size());
		
		// The observation record mentioned previously should exist within the contained resources of the measure report
		assertEquals(1, report.getContained().size());
		assertTrue(report.getContained().get(0) instanceof Observation);
		
		Observation obs = (Observation) report.getContained().get(0);
		
		// For a single patient, the code of the observation should be the supplemental data text
		assertEquals(SDE_RACE, obs.getCode().getText());
		
		// For a single patient, the value of the observation should be the result of the appropriate define
		assertTrue(obs.getValue() instanceof CodeableConcept);
		assertEquals(WHITE_CODE, ((CodeableConcept)obs.getValue()).getCoding().get(0).getCode());
	}
	
	private Patient mockPatient() {
		Patient patient = Mockito.mock(Patient.class);
		IdType patientId = new IdType();
		patientId.setId("testId");
		Mockito.when(patient.getIdElement()).thenReturn(patientId);
		
		return patient;
	}
	
	private Map<String, Map<String, Integer>> getSDEAccumulators(String defineName, String text, Object expressionRetval, Map<String, Map<String, Integer>> initialAccumulators) {
		Map<String, Map<String, Integer>> sdeAccumulators = initialAccumulators;
        
		ExpressionDef mockExpressionDef = Mockito.mock(ExpressionDef.class);
		CDMContext context = Mockito.mock(CDMContext.class);
        
		Mockito.when(context.resolveExpressionRef(defineName)).thenReturn(mockExpressionDef);
		Mockito.when(mockExpressionDef.evaluate(context)).thenReturn(expressionRetval);
        
		MeasureSupplementalDataEvaluation.populateSDEAccumulators(context, mockPatient(), sdeAccumulators, Arrays.asList(createSupplementalDataComponent(defineName, text)));
		
		return sdeAccumulators;
	}
	
	private Code getMaleCode() {
		Code male = new Code();
		male.setCode(MALE_CODE);
		male.setSystem(AdministrativeGender.MALE.getSystem());
		male.setDisplay(AdministrativeGender.MALE.getDisplay());
		
		return male;
	}
	
	private Coding getWhiteCoding() {
		Coding coding = new Coding();
		coding.setCode(WHITE_CODE);
		coding.setDisplay("White");
		coding.setSystem("urn:oid:2.16.840.1.113883.6.238");
		
		return coding;
	}
	
	private Coding getAsianCoding() {
		Coding coding = new Coding();
		coding.setCode(ASIAN_CODE);
		coding.setDisplay("Asian");
		coding.setSystem("urn:oid:2.16.840.1.113883.6.238");
		
		return coding;
	}
	
	private Map<String, Map<String, Integer>> getSexSDEAccumulators() {
		return getSDEAccumulators("SDE Sex", MeasureSupplementalDataEvaluation.SDE_SEX, getMaleCode(), new HashMap<>());
	}
	
	private Map<String, Map<String, Integer>> getSexSDEAccumulatorsWithInitialAccumulators(Map<String, Map<String, Integer>> initialAccumulators) {
		return getSDEAccumulators("SDE Sex", MeasureSupplementalDataEvaluation.SDE_SEX, getMaleCode(), initialAccumulators);
	}
	
	private Map<String, Map<String, Integer>> getSDEAccumulators(String defineName, String text) {
		return getSDEAccumulators(defineName, text, getMaleCode(), new HashMap<>());
	}
	
	private Map<String, Map<String, Integer>> getSexSDEAccumulatorsWithCode(Code code) {
		return getSDEAccumulators("SDE Sex", MeasureSupplementalDataEvaluation.SDE_SEX, code, new HashMap<>());
	}
}

