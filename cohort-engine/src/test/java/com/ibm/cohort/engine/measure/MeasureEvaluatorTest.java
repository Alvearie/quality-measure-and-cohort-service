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
import static org.junit.Assert.assertFalse;
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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.HumanName;
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

import com.google.common.collect.Lists;
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
	
	private Library setupDefineReturnLibrary() throws Exception {
		Library fhirHelpers = mockLibraryRetrieval("FHIRHelpers", DEFAULT_VERSION, "cql/fhir-measure/FHIRHelpers.cql");
		Library library2 = mockLibraryRetrieval("TestAdultMales2", DEFAULT_VERSION, "cql/fhir-measure/test-adult-males2.cql");
		Library library3 = mockLibraryRetrieval("TestAdultMales3", DEFAULT_VERSION, "cql/fhir-measure/test-adult-males3.cql");
		Library library = mockLibraryRetrieval("TestAdultMales", DEFAULT_VERSION, "cql/fhir-measure/test-adult-males.cql");

		library.addRelatedArtifact(asRelation(fhirHelpers));
		library.addRelatedArtifact(asRelation(library2));
		library.addRelatedArtifact(asRelation(library3));
		
		return library;
	}
	
	@Test
	public void in_populations_defines_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		
		//Add 2 names to test list
		HumanName name1 = new HumanName();
		name1.setFamily("Jones");
		HumanName name2 = new HumanName();
		name2.setFamily("Smith");
		patient.setName(Lists.newArrayList(name1, name2));
		
		// Add marital status to test codeable concept
		CodeableConcept maritalStatus = new CodeableConcept();
		Coding maritalCoding = new Coding();
		maritalCoding.setCode("M");
		maritalCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus");
		maritalCoding.setDisplay("Married");
		
		maritalStatus.setCoding(Lists.newArrayList(maritalCoding));
		maritalStatus.setText("Married");
		
		patient.setMaritalStatus(maritalStatus);
		
		mockFhirResourceRetrieval(patient);
		Library library = setupDefineReturnLibrary();
		
		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions(true, true));
		
		assertNotNull(report);
		
		assertFalse(report.getEvaluatedResource().isEmpty());
		
		List<Extension> returnedExtensions = report.getExtensionsByUrl(CDMMeasureEvaluation.EVIDENCE_URL);
		assertFalse(returnedExtensions.isEmpty());
		assertEquals(30, returnedExtensions.size());
	}
	
	@Test
	public void in_populations_evaluated_define_only_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = setupDefineReturnLibrary();
		
		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions(false, true));
		assertNotNull(report);
		
		assertTrue(report.getEvaluatedResource().isEmpty());
		assertFalse(report.getExtensionsByUrl(CDMMeasureEvaluation.EVIDENCE_URL).isEmpty());
	}
	
	@Test
	public void in_populations_evaluated_resources_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = setupDefineReturnLibrary();
		
		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions(true, false));
		assertNotNull(report);
		
		assertFalse(report.getEvaluatedResource().isEmpty());
		assertEquals(null, report.getExtensionByUrl(CDMMeasureEvaluation.EVIDENCE_URL));
	}
	
	@Test
	public void in_populations_no_evaluated_resources_returned() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = setupDefineReturnLibrary();
		
		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, INITIAL_POPULATION);
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, DENOMINATOR);
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, NUMERATOR);

		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null, new MeasureEvidenceOptions());
		assertNotNull(report);
		
		assertTrue(report.getEvaluatedResource().isEmpty());
		assertEquals(null, report.getExtensionByUrl(CDMMeasureEvaluation.EVIDENCE_URL));
	}
}
