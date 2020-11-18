/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;

import com.ibm.cohort.engine.LibraryFormat;

public class MeasureEvaluatorTest extends BaseMeasureTest {

	private MeasureEvaluator evaluator;
	
	@Before
	public void setUp() {
		super.setUp();
		evaluator = new MeasureEvaluator(client, client, client);
	}

	@Test
	public void elm_in_initial_population___cohort_evaluated_correctly() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", "cql/fhir-measure/test-dummy-populations.xml",
				LibraryFormat.MIME_TYPE_APPLICATION_ELM_XML);

		Measure measure = getCohortMeasure("CohortMeasureName", library, INITIAL_POPULATION);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null);
		assertNotNull(report);
		assertEquals(1, report.getGroupFirstRep().getPopulationFirstRep().getCount());

		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));

		// These ensure that the cache is working
		verify(1, getRequestedFor(urlEqualTo("/Library/TestDummyPopulations")));
		verify(1, getRequestedFor(urlEqualTo("/Library?name=" + library.getName() + "&version=1.0.0&_sort=-date")));
	}

	@Test
	public void elm_and_cql_in_initial_population___cohort_evaluated_correctly() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", "cql/fhir-measure/test-dummy-populations.cql",
				"text/cql", "cql/fhir-measure/test-dummy-populations.xml", "application/elm+xml");

		Measure measure = getCohortMeasure("CohortMeasureName", library, INITIAL_POPULATION);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null);
		assertNotNull(report);
		assertEquals(1, report.getGroupFirstRep().getPopulationFirstRep().getCount());
	}

	@Test
	public void in_initial_population_denominator_and_nothing_else___proportion_evaluated_correctly() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", "cql/fhir-measure/test-dummy-populations.cql");

		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null);
		assertNotNull(report);
		assertEquals(expressionsByPopulationType.size(), report.getGroupFirstRep().getPopulation().size());
		for (MeasureReport.MeasureReportGroupPopulationComponent pop : report.getGroupFirstRep().getPopulation()) {
			MeasurePopulationType type = MeasurePopulationType.fromCode(pop.getCode().getCodingFirstRep().getCode());
			switch (type) {
			case INITIALPOPULATION:
				assertEquals(1, pop.getCount());
				break;
			case DENOMINATOR:
				assertEquals(1, pop.getCount());
				break;
			case NUMERATOR:
				assertEquals(0, pop.getCount());
				break;
			default:
				fail("Unexpected population type in result");
			}
		}
	}

	@Test
	public void in_initial_population_and_denominator___caregaps_evaluated_correctly() throws Exception {

		Map<String, Integer> careGapExpectations = new HashMap<>();
		careGapExpectations.put("CareGap1", 0);
		careGapExpectations.put("CareGap2", 1);

		runCareGapTest(null, careGapExpectations);
	}

	@Test
	public void not_in_initial_population___caregaps_evaluated_correctly() throws Exception {

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("InInitialPopulation", Boolean.FALSE);

		expectationsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, 0);
		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOR, 0);

		Map<String, Integer> expectationsByCareGap = new HashMap<>();
		expectationsByCareGap.put("CareGap1", 0);
		expectationsByCareGap.put("CareGap2", 0);

		runCareGapTest(parameters, expectationsByCareGap);
	}

	@Test
	public void not_in_denominator___caregaps_evaluated_correctly() throws Exception {

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("InDenominator", Boolean.FALSE);

		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOR, 0);

		Map<String, Integer> expectationsByCareGap = new HashMap<>();
		expectationsByCareGap.put("CareGap1", 0);
		expectationsByCareGap.put("CareGap2", 0);

		runCareGapTest(parameters, expectationsByCareGap);
	}

	@Test
	public void in_denominator_and_denominator_exclusions___caregaps_evaluated_correctly() throws Exception {

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("InDenominator", Boolean.TRUE);
		parameters.put("InDenominatorExclusion", Boolean.TRUE);

		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOR, 0);
		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOREXCLUSION, 1);

		Map<String, Integer> expectationsByCareGap = new HashMap<>();
		expectationsByCareGap.put("CareGap1", 0);
		expectationsByCareGap.put("CareGap2", 0);

		runCareGapTest(parameters, expectationsByCareGap);
	}

	@Test
	public void in_numerator_and_numerator_exclusions___caregaps_evaluated_correctly() throws Exception {

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("InNumerator", Boolean.TRUE);
		parameters.put("InNumeratorExclusion", Boolean.TRUE);

		expectationsByPopulationType.put(MeasurePopulationType.NUMERATOR, 0);
		expectationsByPopulationType.put(MeasurePopulationType.NUMERATOREXCLUSION, 1);

		Map<String, Integer> expectationsByCareGap = new HashMap<>();
		expectationsByCareGap.put("CareGap1", 0);
		expectationsByCareGap.put("CareGap2", 1);

		runCareGapTest(parameters, expectationsByCareGap);
	}
	
	@Test
	public void in_one_initial_population_for_two_measures___single_measure_report_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", "cql/fhir-measure/test-dummy-populations.cql");

		Measure measure1 = getCareGapMeasure("ProportionMeasureName1", library, expressionsByPopulationType, "CareGap1",
											"CareGap2");
		mockFhirResourceRetrieval(measure1);

		Measure measure2 = getCareGapMeasure("ProportionMeasureName2", library, expressionsByPopulationType, "CareGap1",
											"CareGap2");
		mockFhirResourceRetrieval(measure2);

		Map<String, Object> passingParameters = new HashMap<>();
		passingParameters.put("InInitialPopulation", Boolean.TRUE);

		Map<String, Object> failingParameters = new HashMap<>();
		passingParameters.put("InInitialPopulation", Boolean.FALSE);

		List<MeasureContext> measureContexts = new ArrayList<>();
		measureContexts.add(new MeasureContext(measure1.getId(), passingParameters));
		measureContexts.add(new MeasureContext(measure2.getId(), failingParameters));

		assertEquals(1, evaluator.evaluatePatientMeasures(patient.getId(), measureContexts).size());
	}

	private void runCareGapTest(Map<String, Object> parameters, Map<String, Integer> careGapExpectations)
			throws ParseException, Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", "cql/fhir-measure/test-dummy-populations.cql");

		Measure measure = getCareGapMeasure("ProportionMeasureName", library, expressionsByPopulationType, "CareGap1",
				"CareGap2");
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), parameters);
		assertNotNull(report);
		assertEquals(measure.getGroupFirstRep().getPopulation().size(),
				report.getGroupFirstRep().getPopulation().size());
		List<MeasureReport.MeasureReportGroupPopulationComponent> careGapPopulations = verifyStandardPopulationCounts(
				report);

		assertEquals(2, careGapPopulations.size());
		for (MeasureReport.MeasureReportGroupPopulationComponent pop : careGapPopulations) {
			assertEquals(CDMMeasureEvaluation.CDM_CODE_SYSTEM_MEASURE_POPULATION_TYPE,
					pop.getCode().getCodingFirstRep().getSystem());
			assertEquals(CDMMeasureEvaluation.CARE_GAP, pop.getCode().getCodingFirstRep().getCode());
			assertTrue(pop.getId().startsWith("CareGap")); // this is part of the test fixture and may not match
															// production behavior
			assertEquals(pop.getId(), careGapExpectations.get(pop.getId()).intValue(), pop.getCount());
		}
	}
}
