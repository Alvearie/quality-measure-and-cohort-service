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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
	private MeasureEvaluator evaluator;

	@Before
	public void setUp() {
		FhirServerConfig config = getFhirServerConfig();
		IGenericClient client = FhirClientFactory.newInstance(fhirContext).createFhirClient(config);

		evaluator = new MeasureEvaluator(client, client, client);
	}

	@Test
	public void testCohortMeasureEvaluationSuccess() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = getLibrary("TestDummyPopulations", "cql/fhir-measure/test-dummy-populations.cql");
		mockFhirResourceRetrieval(library);
		
		Bundle bundle = getBundle(library);
		mockFhirResourceRetrieval("/Library?name=" + library.getName() + "&_sort=-date", bundle);
		mockFhirResourceRetrieval("/Library?name=" + library.getName() + "&version=1.0.0&_sort=-date", bundle);

		Measure measure = getCohortMeasure("CohortMeasureName", library, "InitialPopulation");
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null);
		assertNotNull(report);
		assertEquals( 1, report.getGroupFirstRep().getPopulationFirstRep().getCount());
		
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
		// TODO: cache resolved Libraries to avoid multiple REST calls
		verify(2, getRequestedFor(urlEqualTo("/Library/TestDummyPopulations")));
		verify(1, getRequestedFor(urlEqualTo("/Library?name=" + library.getName() + "&_sort=-date")));
		verify(1, getRequestedFor(urlEqualTo("/Library?name=" + library.getName() + "&version=1.0.0&_sort=-date")));
	}
	
	@Test
	public void testProportionMeasureEvaluation() throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata", metadata);

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval(patient);

		Library library = getLibrary("TestDummyPopulations", "cql/fhir-measure/test-dummy-populations.cql");
		mockFhirResourceRetrieval(library);
		
		Bundle bundle = getBundle(library);
		mockFhirResourceRetrieval("/Library?name=" + library.getName() + "&_sort=-date", bundle);
		mockFhirResourceRetrieval("/Library?name=" + library.getName() + "&version=1.0.0&_sort=-date", bundle);

		Map<MeasurePopulationType, String> expressionsByPopulationType = new HashMap<>();
		expressionsByPopulationType.put( MeasurePopulationType.INITIALPOPULATION, "InitialPopulation");
		expressionsByPopulationType.put( MeasurePopulationType.DENOMINATOR, "Denominator");
		expressionsByPopulationType.put( MeasurePopulationType.NUMERATOR, "Numerator");
		
		Measure measure = getProportionMeasure("ProportionMeasureName", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);

		MeasureReport report = evaluator.evaluatePatientMeasure(measure.getId(), patient.getId(), null);
		assertNotNull(report);
		assertEquals( expressionsByPopulationType.size(), report.getGroupFirstRep().getPopulation().size());
		for( MeasureReport.MeasureReportGroupPopulationComponent pop : report.getGroupFirstRep().getPopulation() ) {
			MeasurePopulationType type = MeasurePopulationType.fromCode( pop.getCode().getCodingFirstRep().getCode() );
			switch( type ) {
			case INITIALPOPULATION:
				assertEquals( 1, pop.getCount() );
				break;
			case DENOMINATOR:
				assertEquals( 1, pop.getCount() );
				break;
			case NUMERATOR:
				assertEquals( 0, pop.getCount() );
				break;
			default:
				fail("Unexpected population type in result");
			}
		}
	}
	
	//TODO: test behavior when measure library cannot be resolved

	private Bundle getBundle(Resource... resources) {
		Bundle bundle = new Bundle();
		bundle.setId( UUID.randomUUID().toString() );
		bundle.setTotal(1);
		for( Resource resource : resources ) {
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
	
	public Measure getProportionMeasure(String measureName, Library library, Map<MeasurePopulationType, String> expressionsByPopType) {
		Measure measure = getTemplateMeasure(measureName, library, MeasureScoring.PROPORTION);
		
		Measure.MeasureGroupComponent group = new Measure.MeasureGroupComponent();
		addPopulations(group, expressionsByPopType);
		measure.addGroup(group);
		
		return measure;
	}

	private void addPopulations(Measure.MeasureGroupComponent group,
			Map<MeasurePopulationType, String> expressionsByPopType) {
		for( Map.Entry<MeasurePopulationType, String> entry : expressionsByPopType.entrySet() ) {
			Measure.MeasureGroupPopulationComponent pop = new Measure.MeasureGroupPopulationComponent();
			pop.setCode( new CodeableConcept().addCoding( new Coding().setCode(entry.getKey().toCode())) );
			pop.setCriteria( new Expression().setExpression(entry.getValue()));
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
