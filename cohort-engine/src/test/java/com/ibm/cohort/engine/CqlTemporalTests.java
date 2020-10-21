/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
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
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class CqlTemporalTests {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(options().port(8089)/* .notifier(new ConsoleNotifier(true)) */);

	private final Condition CONDITION_IN = getCondition(2015, 1, 10);

	private final Encounter ENCOUNTER_1 = getEncounter(2015, 1, 1);
	private final Encounter ENCOUNTER_2 = getEncounter(2015, 1, 15);
	private final Encounter ENCOUNTER_3 = getEncounter(2017, 1, 1);

	@Test
	public void confirmCanFindEventAfterSecondEvent() throws Exception {
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Observation observationIN = new Observation();
		DateTimeType observationEffective = new DateTimeType(new Date());
		observationEffective.setYear(2015);
		observationEffective.setMonth(1);
		observationEffective.setDay(15);
		observationIN.setEffective(observationEffective);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_1.xml", "cql/includes/FHIRHelpers.xml");

		CqlTestUtil.mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", CqlTestUtil.getFhirParser(), CONDITION_IN, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", CqlTestUtil.getFhirParser(), ENCOUNTER_1, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", CqlTestUtil.getFhirParser(), observationIN, fhirConfig);

		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("Observations Exist")), Arrays.asList("123"), (patientId, expression, result) -> {
					assertEquals("Observations Exist", expression);
					assertEquals(Boolean.TRUE, result);
				});
	}

	@Test
	public void doesNotFindEventAfterSecondEvent() throws Exception {
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Observation observationOUT = new Observation();
		DateTimeType observationEffective2 = new DateTimeType(new Date());
		observationEffective2.setYear(2017);
		observationEffective2.setMonth(1);
		observationEffective2.setDay(15);
		observationOUT.setEffective(observationEffective2);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_1.xml", "cql/includes/FHIRHelpers.xml");

		CqlTestUtil.mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", CqlTestUtil.getFhirParser(), CONDITION_IN, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", CqlTestUtil.getFhirParser(), ENCOUNTER_1, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", CqlTestUtil.getFhirParser(), observationOUT, fhirConfig);

		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("Observations Exist")), Arrays.asList("123"), (patientId, expression, result) -> {
					assertEquals("Observations Exist", expression);
					assertEquals(Boolean.FALSE, result);
				});
	}

	@Test
	public void confirmCanFindEventWithExistenceOfThirdEvent() throws Exception {
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Observation observation = new Observation();
		DateTimeType observationEffective = new DateTimeType(new Date());
		observationEffective.setYear(2015);
		observationEffective.setMonth(0);
		observationEffective.setDay(5);
		observation.setEffective(observationEffective);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_2.xml", "cql/includes/FHIRHelpers.xml");

		CqlTestUtil.mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", CqlTestUtil.getFhirParser(), CONDITION_IN, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", CqlTestUtil.getFhirParser(), ENCOUNTER_1, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", CqlTestUtil.getFhirParser(), observation, fhirConfig);

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
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Observation observation = new Observation();
		DateTimeType observationEffective = new DateTimeType(new Date());
		observationEffective.setYear(2015);
		observationEffective.setMonth(2);
		observationEffective.setDay(5);
		observation.setEffective(observationEffective);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_2.xml", "cql/includes/FHIRHelpers.xml");

		CqlTestUtil.mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", CqlTestUtil.getFhirParser(), CONDITION_IN, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", CqlTestUtil.getFhirParser(), ENCOUNTER_1, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", CqlTestUtil.getFhirParser(), observation, fhirConfig);

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
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);


		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_3.xml", "cql/includes/FHIRHelpers.xml");

		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent firstEncounter = new Bundle.BundleEntryComponent();
		firstEncounter.setResource(ENCOUNTER_1);
		Bundle.BundleEntryComponent secondEncounter = new Bundle.BundleEntryComponent();
		secondEncounter.setResource(ENCOUNTER_2);
		bundle.addEntry(firstEncounter);
		bundle.addEntry(secondEncounter);

		CqlTestUtil.mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", CqlTestUtil.getFhirParser(), bundle, fhirConfig);
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
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);


		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_3.xml", "cql/includes/FHIRHelpers.xml");

		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent firstEncounter = new Bundle.BundleEntryComponent();
		firstEncounter.setResource(ENCOUNTER_1);
		Bundle.BundleEntryComponent secondEncounter = new Bundle.BundleEntryComponent();
		secondEncounter.setResource(ENCOUNTER_3);
		bundle.addEntry(firstEncounter);
		bundle.addEntry(secondEncounter);

		CqlTestUtil.mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", CqlTestUtil.getFhirParser(), bundle, fhirConfig);
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
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);


		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_4.xml", "cql/includes/FHIRHelpers.xml");

		CqlTestUtil.mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", CqlTestUtil.getFhirParser(), ENCOUNTER_3, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", CqlTestUtil.getFhirParser(), CONDITION_IN, fhirConfig);
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
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_4.xml", "cql/includes/FHIRHelpers.xml");

		CqlTestUtil.mockFhirResourceRetrieval("/Encounter?subject=Patient%2F123", CqlTestUtil.getFhirParser(), ENCOUNTER_1, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", CqlTestUtil.getFhirParser(), CONDITION_IN, fhirConfig);
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
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

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

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_5.xml", "cql/includes/FHIRHelpers.xml");

		CqlTestUtil.mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", CqlTestUtil.getFhirParser(), bundle, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", CqlTestUtil.getFhirParser(), CONDITION_IN, fhirConfig);
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
		Patient patient = CqlTestUtil.getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

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

		CqlEngineWrapper wrapper = CqlTestUtil.setupTestFor(patient, fhirConfig,"cql/temporal/test_5.xml", "cql/includes/FHIRHelpers.xml");

		CqlTestUtil.mockFhirResourceRetrieval("/Observation?subject=Patient%2F123", CqlTestUtil.getFhirParser(), bundle, fhirConfig);
		CqlTestUtil.mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", CqlTestUtil.getFhirParser(), CONDITION_IN, fhirConfig);
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
		Date encounterDate = new Date(year-1900, month - 1, day);
		encounterPeriod.setStart(encounterDate);
		encounterPeriod.setEnd(encounterDate);
		encounter.setPeriod(encounterPeriod);
		return encounter;
	}
}
