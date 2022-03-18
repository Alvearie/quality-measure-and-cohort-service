/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;
import com.ibm.cohort.cql.evaluation.CqlEvaluator;
import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlVersionedIdentifier;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.junit.Assert;
import org.junit.Test;

import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import java.util.Calendar;
import java.util.Map;

public class CqlTemporalTests extends PatientTestBase {

	private final Condition CONDITION_IN = getCondition(2015, 1, 10);

	private final Encounter ENCOUNTER_1 = getEncounter(2015, 1, 1);
	private final Encounter ENCOUNTER_2 = getEncounter(2015, 1, 15);
	private final Encounter ENCOUNTER_3 = getEncounter(2017, 1, 1);

	@Test
	public void confirmCanFindEventAfterSecondEvent() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Observation observationIN = new Observation();
		DateTimeType observationEffective = new DateTimeType(new Date());
		observationEffective.setYear(2015);
		observationEffective.setMonth(1);
		observationEffective.setDay(15);
		observationIN.setEffective(observationEffective);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", getFhirParser(), CONDITION_IN, fhirConfig);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123&_format=json", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123&_format=json", getFhirParser(), observationIN, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "Observations Exist";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test1", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, true);

		Assert.assertEquals(expected, actual.getExpressionResults());

	}

	@Test
	public void doesNotFindEventAfterSecondEvent() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Observation observationOUT = new Observation();
		DateTimeType observationEffective2 = new DateTimeType(new Date());
		observationEffective2.setYear(2017);
		observationEffective2.setMonth(1);
		observationEffective2.setDay(15);
		observationOUT.setEffective(observationEffective2);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", getFhirParser(), CONDITION_IN, fhirConfig);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123&_format=json", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123&_format=json", getFhirParser(), observationOUT, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "Observations Exist";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test1", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, false);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	@Test
	public void confirmCanFindEventWithExistenceOfThirdEvent() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Observation observation = new Observation();
		DateTimeType observationEffective = new DateTimeType(new Date());
		observationEffective.setYear(2015);
		observationEffective.setMonth(0);
		observationEffective.setDay(5);
		observation.setEffective(observationEffective);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", getFhirParser(), CONDITION_IN, fhirConfig);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123&_format=json", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123&_format=json", getFhirParser(), observation, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "ObservationWithin30DaysOfCondition";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test2", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, true);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	@Test
	public void cannotFindEventWithoutExistenceOfThirdEvent() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Observation observation = new Observation();
		DateTimeType observationEffective = new DateTimeType(new Date());
		observationEffective.setYear(2015);
		observationEffective.setMonth(2);
		observationEffective.setDay(5);
		observation.setEffective(observationEffective);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", getFhirParser(), CONDITION_IN, fhirConfig);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123&_format=json", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123&_format=json", getFhirParser(), observation, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "ObservationWithin30DaysOfCondition";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test2", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, false);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	@Test
	public void canFindMultipleEncountersFollowingEachOther() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent firstEncounter = new Bundle.BundleEntryComponent();
		firstEncounter.setResource(ENCOUNTER_1);
		Bundle.BundleEntryComponent secondEncounter = new Bundle.BundleEntryComponent();
		secondEncounter.setResource(ENCOUNTER_2);
		bundle.addEntry(firstEncounter);
		bundle.addEntry(secondEncounter);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123&_format=json", getFhirParser(), bundle, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "ValidEncounters2";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test3", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, true);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	@Test
	public void cannotFindEncountersThatDontExist() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent firstEncounter = new Bundle.BundleEntryComponent();
		firstEncounter.setResource(ENCOUNTER_1);
		Bundle.BundleEntryComponent secondEncounter = new Bundle.BundleEntryComponent();
		secondEncounter.setResource(ENCOUNTER_3);
		bundle.addEntry(firstEncounter);
		bundle.addEntry(secondEncounter);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123&_format=json", getFhirParser(), bundle, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "ValidEncounters2";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test3", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, false);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	@Test
	public void determineIfAnEventFollows() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123&_format=json", getFhirParser(), ENCOUNTER_3, fhirConfig);
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", getFhirParser(), CONDITION_IN, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "NotFollowedByCondition";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test4", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, true);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	@Test
	public void anEventDoesFollow() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123&_format=json", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", getFhirParser(), CONDITION_IN, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "NotFollowedByCondition";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test4", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, false);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	@Test
	public void eventHappensWithin4Days() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		Observation observationIN = new Observation();
		DateTimeType observationEffective = new DateTimeType(new Date());
		observationEffective.setYear(2015);
		observationEffective.setMonth(1);
		observationEffective.setDay(15);
		observationIN.setEffective(observationEffective);
		observationIN.setId("FIRST");

		Observation observationOUT = new Observation();
		DateTimeType observationEffective2 = new DateTimeType(new Date());
		observationEffective2.setYear(2015);
		observationEffective2.setMonth(1);
		observationEffective2.setDay(16);
		observationOUT.setEffective(observationEffective2);
		observationOUT.setId("SECOND");

		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent firstEncounter = new Bundle.BundleEntryComponent();
		firstEncounter.setResource(observationIN);
		Bundle.BundleEntryComponent secondEncounter = new Bundle.BundleEntryComponent();
		secondEncounter.setResource(observationOUT);
		bundle.addEntry(firstEncounter);
		bundle.addEntry(secondEncounter);

		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123&_format=json", getFhirParser(), bundle, fhirConfig);
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", getFhirParser(), CONDITION_IN, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "ValidObservation within 4 days";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test5", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, true);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	@Test
	public void noEventWithinFourDays() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		Observation observationIN = new Observation();
		DateTimeType observationEffective = new DateTimeType(new Date());
		observationEffective.setYear(2015);
		observationEffective.setMonth(1);
		observationEffective.setDay(15);
		observationIN.setEffective(observationEffective);

		Observation observationOUT = new Observation();
		DateTimeType observationEffective2 = new DateTimeType(new Date());
		observationEffective2.setYear(2015);
		observationEffective2.setMonth(1);
		observationEffective2.setDay(25);
		observationOUT.setEffective(observationEffective2);

		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent firstEncounter = new Bundle.BundleEntryComponent();
		firstEncounter.setResource(observationIN);
		Bundle.BundleEntryComponent secondEncounter = new Bundle.BundleEntryComponent();
		secondEncounter.setResource(observationOUT);
		bundle.addEntry(firstEncounter);
		bundle.addEntry(secondEncounter);

		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123&_format=json", getFhirParser(), bundle, fhirConfig);
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", getFhirParser(), CONDITION_IN, fhirConfig);

		CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal", ClasspathCqlLibraryProvider.FHIR_HELPERS_CLASSPATH);
		String expression = "ValidObservation not within 4 days";

		CqlEvaluationResult actual = evaluator.evaluate(
				new CqlVersionedIdentifier("Test5", "1.0.0"),
				null,
				newPatientContext("123"),
				Collections.singleton(expression)
		);
		Map<String, Object> expected = new HashMap<>();
		expected.put(expression, true);

		Assert.assertEquals(expected, actual.getExpressionResults());
	}

	public Condition getCondition(int year, int month, int day) {
		Condition condition = new Condition();
		DateTimeType conditionOnset = new DateTimeType(new Date());
		conditionOnset.setYear(year);
		conditionOnset.setMonth(month - 1);
		conditionOnset.setDay(day);
		condition.setOnset(conditionOnset);
		condition.setAbatement(conditionOnset);
		return condition;
	}

	public Encounter getEncounter(int year, int month, int day) {
		Encounter encounter = new Encounter();
		Period encounterPeriod = new Period();
		Calendar c = Calendar.getInstance();
		c.set(year, month-1, day);
		Date encounterDate = c.getTime();
		encounterPeriod.setStart(encounterDate);
		encounterPeriod.setEnd(encounterDate);
		encounter.setPeriod(encounterPeriod);
		return encounter;
	}
}
