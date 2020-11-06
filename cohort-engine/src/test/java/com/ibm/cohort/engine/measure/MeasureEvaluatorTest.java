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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.codesystems.MeasureScoring;
import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;

import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.engine.FhirClientFactory;
import com.ibm.cohort.engine.FhirServerConfig;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureEvaluatorTest extends BaseFhirTest {

	private static final String NUMERATOR_EXCLUSION = "Numerator Exclusion";

	private static final String NUMERATOR = "Numerator";

	private static final String DENOMINATOR_EXCEPTION = "Denominator Exception";

	private static final String DENOMINATOR_EXCLUSION = "Denominator Exclusion";

	private static final String DENOMINATOR = "Denominator";

	private static final String INITIAL_POPULATION = "Initial Population";

	private MeasureEvaluator evaluator;
	protected Map<MeasurePopulationType, String> expressionsByPopulationType;
	protected Map<MeasurePopulationType, Integer> expectationsByPopulationType;

	@Before
	public void setUp() {
		FhirServerConfig config = getFhirServerConfig();
		IGenericClient client = FhirClientFactory.newInstance(fhirContext).createFhirClient(config);

		evaluator = new MeasureEvaluator(client, client, client);

		expressionsByPopulationType = new HashMap<>();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOREXCLUSION, DENOMINATOR_EXCLUSION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOREXCEPTION, DENOMINATOR_EXCEPTION);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOREXCLUSION, NUMERATOR_EXCLUSION);

		expectationsByPopulationType = new HashMap<>();
		expectationsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, 1);
		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOR, 1);
		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOREXCLUSION, 0);
		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOREXCEPTION, 0);
		expectationsByPopulationType.put(MeasurePopulationType.NUMERATOR, 0);
		expectationsByPopulationType.put(MeasurePopulationType.NUMERATOREXCLUSION, 0);

	}

	@Test
	public void elm_in_initial_population___cohort_evaluated_correctly() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", "cql/fhir-measure/test-dummy-populations.xml",
				"application/elm+xml");

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

	private List<MeasureReport.MeasureReportGroupPopulationComponent> verifyStandardPopulationCounts(
			MeasureReport report) {
		List<MeasureReport.MeasureReportGroupPopulationComponent> careGapPopulations = new ArrayList<>();
		for (MeasureReport.MeasureReportGroupPopulationComponent pop : report.getGroupFirstRep().getPopulation()) {
			MeasurePopulationType type = MeasurePopulationType.fromCode(pop.getCode().getCodingFirstRep().getCode());
			if (type != null) {
				assertEquals(type.toCode(), expectationsByPopulationType.get(type).intValue(), pop.getCount());
			} else {
				careGapPopulations.add(pop);
			}
		}
		return careGapPopulations;
	}

	protected Library mockLibraryRetrieval(String libraryName, String... cqlResource) throws Exception {
		Library library = getLibrary(libraryName, cqlResource);
		mockFhirResourceRetrieval(library);

		Bundle bundle = getBundle(library);
		mockFhirResourceRetrieval("/Library?name=" + library.getName() + "&_sort=-date", bundle);
		mockFhirResourceRetrieval("/Library?name=" + library.getName() + "&version=1.0.0&_sort=-date", bundle);
		return library;
	}

	// TODO: test behavior when measure library cannot be resolved

	protected Bundle getBundle(Resource... resources) {
		Bundle bundle = new Bundle();
		bundle.setId(UUID.randomUUID().toString());
		bundle.setTotal(1);
		for (Resource resource : resources) {
			bundle.getEntry().add(new Bundle.BundleEntryComponent().setResource(resource));
		}
		return bundle;
	}

	public Measure getCohortMeasure(String measureName, Library library, String expression) {
		Measure measure = getTemplateMeasure(measureName, library, MeasureScoring.COHORT);

		Measure.MeasureGroupComponent group = new Measure.MeasureGroupComponent();
		addPopulations(group, Collections.singletonMap(MeasurePopulationType.INITIALPOPULATION, expression));
		measure.addGroup(group);

		return measure;
	}

	public Measure getProportionMeasure(String measureName, Library library,
			Map<MeasurePopulationType, String> expressionsByPopType) {
		Measure measure = getTemplateMeasure(measureName, library, MeasureScoring.PROPORTION);

		Measure.MeasureGroupComponent group = new Measure.MeasureGroupComponent();
		addPopulations(group, expressionsByPopType);
		measure.addGroup(group);

		return measure;
	}

	public Measure getCareGapMeasure(String measureName, Library library,
			Map<MeasurePopulationType, String> expressionsByPopType, String... careGapExpressions) {
		Measure measure = getProportionMeasure(measureName, library, expressionsByPopType);

		assertNotNull(careGapExpressions);
		for (String expression : careGapExpressions) {
			Measure.MeasureGroupPopulationComponent pop = new Measure.MeasureGroupPopulationComponent();
			pop.setId(expression);
			pop.setCode(new CodeableConcept(new Coding(CDMMeasureEvaluation.CDM_CODE_SYSTEM_MEASURE_POPULATION_TYPE,
					CDMMeasureEvaluation.CARE_GAP, "Care Gap")));
			pop.setCriteria(new Expression().setLanguage("text/cql+identifier").setExpression(expression));
			measure.getGroupFirstRep().addPopulation(pop);
		}

		return measure;
	}

	private void addPopulations(Measure.MeasureGroupComponent group,
			Map<MeasurePopulationType, String> expressionsByPopType) {
		for (Map.Entry<MeasurePopulationType, String> entry : expressionsByPopType.entrySet()) {
			Measure.MeasureGroupPopulationComponent pop = new Measure.MeasureGroupPopulationComponent();
			pop.setCode(new CodeableConcept().addCoding(new Coding().setCode(entry.getKey().toCode())));
			pop.setCriteria(new Expression().setExpression(entry.getValue()));
			group.addPopulation(pop);
		}
	}

	public Measure getTemplateMeasure(String measureName, Library library, MeasureScoring scoring) {
		Measure measure = new Measure();
		measure.setId(measureName);
		measure.setName(measureName);
		measure.setDate(new Date());
		measure.setLibrary(Arrays.asList(asCanonical(library)));
		measure.setScoring(new CodeableConcept().addCoding(new Coding().setCode(scoring.toCode())));
		return measure;
	}
}
