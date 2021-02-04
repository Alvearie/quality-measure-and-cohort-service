/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.junit.Test;

import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import java.util.Calendar;

public class CqlTemporalTests extends BasePatientTest {

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

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_1.xml", "cql/includes/FHIRHelpers.xml");

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), CONDITION_IN, fhirConfig);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", getFhirParser(), observationIN, fhirConfig);

		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("Observations Exist")), Arrays.asList("123"), (patientId, expression, result) -> {
					assertEquals("Observations Exist", expression);
					assertEquals(Boolean.TRUE, result);
				});
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

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_1.xml", "cql/includes/FHIRHelpers.xml");

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), CONDITION_IN, fhirConfig);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", getFhirParser(), observationOUT, fhirConfig);

		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("Observations Exist")), Arrays.asList("123"), (patientId, expression, result) -> {
					assertEquals("Observations Exist", expression);
					assertEquals(Boolean.FALSE, result);
				});
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

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_2.xml", "cql/includes/FHIRHelpers.xml");

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), CONDITION_IN, fhirConfig);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", getFhirParser(), observation, fhirConfig);

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("ObservationWithin30DaysOfCondition")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("ObservationWithin30DaysOfCondition", expression);
					assertEquals(Boolean.TRUE, result);
				});

		assertEquals(1, count.get());
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

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_2.xml", "cql/includes/FHIRHelpers.xml");

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), CONDITION_IN, fhirConfig);
		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", getFhirParser(), observation, fhirConfig);

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("ObservationWithin30DaysOfCondition")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("ObservationWithin30DaysOfCondition", expression);
					assertEquals(Boolean.FALSE, result);
				});

		assertEquals(1, count.get());
	}

	@Test
	public void canFindMultipleEncountersFollowingEachOther() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);


		FhirServerConfig fhirConfig = getFhirServerConfig();

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_3.xml", "cql/includes/FHIRHelpers.xml");

		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent firstEncounter = new Bundle.BundleEntryComponent();
		firstEncounter.setResource(ENCOUNTER_1);
		Bundle.BundleEntryComponent secondEncounter = new Bundle.BundleEntryComponent();
		secondEncounter.setResource(ENCOUNTER_2);
		bundle.addEntry(firstEncounter);
		bundle.addEntry(secondEncounter);

		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", getFhirParser(), bundle, fhirConfig);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("ValidEncounters2")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("ValidEncounters2", expression);
					assertEquals(Boolean.TRUE, result);
				});

		assertEquals(1, count.get());
	}

	@Test
	public void cannotFindEncountersThatDontExist() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);


		FhirServerConfig fhirConfig = getFhirServerConfig();

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_3.xml", "cql/includes/FHIRHelpers.xml");

		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent firstEncounter = new Bundle.BundleEntryComponent();
		firstEncounter.setResource(ENCOUNTER_1);
		Bundle.BundleEntryComponent secondEncounter = new Bundle.BundleEntryComponent();
		secondEncounter.setResource(ENCOUNTER_3);
		bundle.addEntry(firstEncounter);
		bundle.addEntry(secondEncounter);

		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", getFhirParser(), bundle, fhirConfig);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("ValidEncounters2")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("ValidEncounters2", expression);
					assertEquals(Boolean.FALSE, result);
				});

		assertEquals(1, count.get());
	}

	@Test
	public void determineIfAnEventFollows() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);


		FhirServerConfig fhirConfig = getFhirServerConfig();

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_4.xml", "cql/includes/FHIRHelpers.xml");

		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", getFhirParser(), ENCOUNTER_3, fhirConfig);
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), CONDITION_IN, fhirConfig);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("NotFollowedByCondition")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("NotFollowedByCondition", expression);
					assertEquals(Boolean.TRUE, result);
				});

		assertEquals(1, count.get());
	}

	@Test
	public void anEventDoesFollow() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_4.xml", "cql/includes/FHIRHelpers.xml");

		mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", getFhirParser(), ENCOUNTER_1, fhirConfig);
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), CONDITION_IN, fhirConfig);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("NotFollowedByCondition")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("NotFollowedByCondition", expression);
					assertEquals(Boolean.FALSE, result);
				});

		assertEquals(1, count.get());
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

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_5.xml", "cql/includes/FHIRHelpers.xml");

		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", getFhirParser(), bundle, fhirConfig);
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), CONDITION_IN, fhirConfig);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("ValidObservation within 4 days")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("ValidObservation within 4 days", expression);
					assertEquals(Boolean.TRUE, result);
				});

		assertEquals(1, count.get());
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

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,"cql/temporal/test_5.xml", "cql/includes/FHIRHelpers.xml");

		mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", getFhirParser(), bundle, fhirConfig);
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), CONDITION_IN, fhirConfig);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("ValidObservation not within 4 days")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("ValidObservation not within 4 days", expression);
					assertEquals(Boolean.TRUE, result);
				});

		assertEquals(1, count.get());
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
