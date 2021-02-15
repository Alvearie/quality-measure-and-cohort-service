/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;
import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;

import com.ibm.cohort.engine.LibraryFormat;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.engine.measure.parameter.UnsupportedFhirTypeException;

public class MeasureEvaluatorTest extends BaseMeasureTest {

	public static final String DEFAULT_VERSION = "1.0.0";
	
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

		Library library = mockLibraryRetrieval("TestDummyPopulations", DEFAULT_VERSION, "cql/fhir-measure/test-dummy-populations.xml",
				LibraryFormat.MIME_TYPE_APPLICATION_ELM_XML);

		Measure measure = getCohortMeasure("CohortMeasureName", library, INITIAL_POPULATION);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null);
		assertNotNull(report);
		assertEquals(1, report.getGroupFirstRep().getPopulationFirstRep().getCount());

		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));

		// These ensure that the cache is working
		verify(1, getRequestedFor(urlMatching("/Library\\?url=http.*")));
		verify(1, getRequestedFor(urlEqualTo("/Library?name%3Aexact=" + library.getName() + "&version=1.0.0")));
	}
	
	@Test
	public void elm_and_cql_in_initial_population___cohort_evaluated_correctly() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", DEFAULT_VERSION, "cql/fhir-measure/test-dummy-populations.cql",
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

		Library library = mockLibraryRetrieval("TestDummyPopulations", DEFAULT_VERSION, "cql/fhir-measure/test-dummy-populations.cql");

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
	public void in_one_initial_population_for_two_measures___two_measure_reports_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", DEFAULT_VERSION, "cql/fhir-measure/test-dummy-populations.cql");

		Measure measure1 = getCareGapMeasure("ProportionMeasureName1", library, expressionsByPopulationType, "CareGap1",
											"CareGap2");
		mockFhirResourceRetrieval(measure1);

		Measure measure2 = getCareGapMeasure("ProportionMeasureName2", library, expressionsByPopulationType, "CareGap1",
											"CareGap2");
		mockFhirResourceRetrieval(measure2);

		Map<String, Object> passingParameters = new HashMap<>();
		passingParameters.put("InInitialPopulation", Boolean.TRUE);

		Map<String, Object> failingParameters = new HashMap<>();
		failingParameters.put("InInitialPopulation", Boolean.FALSE);

		List<MeasureContext> measureContexts = new ArrayList<>();
		measureContexts.add(new MeasureContext(measure1.getId(), passingParameters));
		measureContexts.add(new MeasureContext(measure2.getId(), failingParameters));

		assertEquals(2, evaluator.evaluatePatientMeasures(patient.getId(), measureContexts).size());
	}
	
	@Test
	public void multilevel_library_dependencies___successfully_loaded_and_evaluated() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library nestedHelperLibrary = mockLibraryRetrieval("NestedChild", DEFAULT_VERSION, "cql/fhir-measure-nested-libraries/test-nested-child.cql");
		
		Library helperLibrary = mockLibraryRetrieval("Child", DEFAULT_VERSION, "cql/fhir-measure-nested-libraries/test-child.cql");
		helperLibrary.addRelatedArtifact( asRelation(nestedHelperLibrary) );
		
		Library library = mockLibraryRetrieval("Parent", DEFAULT_VERSION, "cql/fhir-measure-nested-libraries/test-parent.cql");
		library.addRelatedArtifact( asRelation(helperLibrary) );
		
		Measure measure = getCareGapMeasure("NestedLibraryMeasure", library, expressionsByPopulationType, "CareGap1",
											"CareGap2");
		mockFhirResourceRetrieval(measure);

		Map<String, Object> passingParameters = new HashMap<>();
		passingParameters.put("InInitialPopulation", Boolean.TRUE);

		List<MeasureContext> measureContexts = new ArrayList<>();
		measureContexts.add(new MeasureContext(measure.getId(), passingParameters));

		List<MeasureReport> reports = evaluator.evaluatePatientMeasures(patient.getId(), measureContexts);
		assertEquals(1, reports.size());
		
		MeasureReport report = reports.get(0);
		verifyStandardPopulationCounts(report);
	}
	
	@Test
	public void id_based_library_link___successfully_loaded_and_evaluated() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);
		
		Library library = mockLibraryRetrieval("TestDummyPopulations", DEFAULT_VERSION, "cql/fhir-measure/test-dummy-populations.cql");
		
		Measure measure = getCareGapMeasure("IDBasedLibraryMeasure", library, expressionsByPopulationType, "CareGap1",
											"CareGap2");
		
		measure.setLibrary( Arrays.asList( new CanonicalType( "Library/" + library.getId()) ) );
		mockFhirResourceRetrieval(measure);

		Map<String, Object> passingParameters = new HashMap<>();
		passingParameters.put("InInitialPopulation", Boolean.TRUE);

		List<MeasureContext> measureContexts = new ArrayList<>();
		measureContexts.add(new MeasureContext(measure.getId(), passingParameters));

		List<MeasureReport> reports = evaluator.evaluatePatientMeasures(patient.getId(), measureContexts);
		assertEquals(1, reports.size());
		
		MeasureReport report = reports.get(0);
		verifyStandardPopulationCounts(report);
		
		verify(1, getRequestedFor(urlEqualTo("/Library/" + library.getId())));
	}
	
	@Test
	public void in_populations_evaluated_resources_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestAdultMales", DEFAULT_VERSION, "cql/fhir-measure/test-adult-males.cql");

		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions(true, true));

		assertNotNull(report);

		assertTrue(!report.getEvaluatedResource().isEmpty());
	}

	@Test
	public void in_populations_no_evaluated_resources_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestAdultMales", DEFAULT_VERSION, "cql/fhir-measure/test-adult-males.cql");

		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions());
		assertNotNull(report);

		assertTrue(!report.getEvaluatedResource().isEmpty());
		
		// When this functionality is implemented, this is what we want to be returned
//		assertTrue(report.getEvaluatedResource().isEmpty());
	}

	@Test
	public void measure_default_valid() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestAdultMales", DEFAULT_VERSION, "cql/fhir-measure/test-parameter-defaults.cql");

		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		Extension parameterExtension = new Extension();
		parameterExtension.setUrl(MeasureEvaluator.PARAMETER_EXTENSION_URL);
		parameterExtension.setId("SomeAge");

		Type age = new IntegerType(20);
		parameterExtension.setValue(age);

		measure.addExtension(parameterExtension);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions());
		assertNotNull(report);

		assertTrue(!report.getEvaluatedResource().isEmpty());
	}

	@Test(expected = InvalidOperatorArgument.class)
	public void measure_default_invalid_type() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestAdultMales", DEFAULT_VERSION, "cql/fhir-measure/test-parameter-defaults.cql");

		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);

		Extension parameterExtension = new Extension();
		parameterExtension.setUrl(MeasureEvaluator.PARAMETER_EXTENSION_URL);
		parameterExtension.setId("SomeAge");
		Type age = new StringType("invalid");

		parameterExtension.setValue(age);

		measure.addExtension(parameterExtension);
		mockFhirResourceRetrieval(measure);

		evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions());
	}

	@Test(expected = UnsupportedFhirTypeException.class)
	public void measure_default_unsupported_type() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestAdultMales", DEFAULT_VERSION, "cql/fhir-measure/test-parameter-defaults.cql");

		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);

		Extension parameterExtension = new Extension();
		parameterExtension.setUrl(MeasureEvaluator.PARAMETER_EXTENSION_URL);
		parameterExtension.setId("SomeAge");

		Address unsupportedType = new Address();
		unsupportedType.setCity("Cleaveland");

		parameterExtension.setValue(unsupportedType);

		measure.addExtension(parameterExtension);
		mockFhirResourceRetrieval(measure);

		evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions());
	}

	private void runCareGapTest(Map<String, Object> parameters, Map<String, Integer> careGapExpectations)
			throws ParseException, Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestDummyPopulations", DEFAULT_VERSION, "cql/fhir-measure/test-dummy-populations.cql");

		Measure measure = getCareGapMeasure("ProportionMeasureName", library, expressionsByPopulationType, "CareGap1",
				"CareGap2");
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), parameters);
		assertNotNull(report);
		assertPopulationExpectations(measure, report, careGapExpectations);
	}

	protected void assertPopulationExpectations(Measure measure, MeasureReport report,
			Map<String, Integer> expectations) {
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
			assertEquals(pop.getId(), expectations.get(pop.getId()).intValue(), pop.getCount());
		}
	}
	

	protected RelatedArtifact asRelation(Library library) {
		return new RelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.DEPENDSON).setResource( library.getUrl() + "|" + library.getVersion() );
	}
	
	@Test
	public void in_populations_evaluated_resources_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestAdultMales", "cql/fhir-measure/test-adult-males.cql");
		Library library2 = mockLibraryRetrieval("TestAdultMales2", "cql/fhir-measure/test-adult-males2.cql");
		Library library3 = mockLibraryRetrieval("TestAdultMales3", "cql/fhir-measure/test-adult-males3.cql");

		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

//		long ms = System.currentTimeMillis();
		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions(true, true));
//		System.out.println("Duration: " + (System.currentTimeMillis() - ms));
//		
//		List<Long> durationWith = new ArrayList<>();
//		for(int i = 0; i < 100; i++) {
//			ms = System.currentTimeMillis();
//			report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions(true, true));
//			durationWith.add(System.currentTimeMillis() - ms);
//		}
//		
//		
//		List<Long> durationWithout = new ArrayList<>();
//		for(int i = 0; i < 100; i++) {
//			ms = System.currentTimeMillis();
//			report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null);
//			durationWithout.add(System.currentTimeMillis() - ms);
//		}
//		
//		System.out.println("Average duration with evidence: " + durationWith.stream().mapToDouble(a -> a).average());
//		System.out.println("Average duration without evidence: " + durationWithout.stream().mapToDouble(a -> a).average());
		
		assertNotNull(report);
		
		assertTrue(report.getEvaluatedResource().size() > 0);
	}
	
	@Test
	public void in_populations_no_evaluated_resources_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("TestAdultMales", "cql/fhir-measure/test-adult-males.cql");

		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions());
		assertNotNull(report);
		
		assertEquals(0, report.getEvaluatedResource().size());
	}
}
