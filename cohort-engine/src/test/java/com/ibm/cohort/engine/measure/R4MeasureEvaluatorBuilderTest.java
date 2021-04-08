/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupPopulationComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.ibm.cohort.engine.measure.cache.DefaultRetrieveCacheContext;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;

public class R4MeasureEvaluatorBuilderTest extends BaseMeasureTest {

	private static final String MARITAL_STATUS_SYSTEM = "http://hl7.org/fhir/ValueSet/marital-status";
	private static final String MARRIED = "M";
	
	private static final String DIABETES_CODESYSTEM = "snomed-ct";
	private static final String DIABETES_CODE = "1234";

	private static final String PATIENT_ID = "FHIRClientContextTest-PatientId";
	private static final String MEASURE_NAME = "FHIRClientContextTest-MeasureName";

	private FHIRClientContext clientContext;

	@Before
	public void setup() throws Exception {
		super.setUp();

		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient p = getPatient(PATIENT_ID, AdministrativeGender.MALE, 22);
		CodeableConcept married = new CodeableConcept();
		married.getCodingFirstRep().setSystem(MARITAL_STATUS_SYSTEM).setCode(MARRIED);
		p.setMaritalStatus(married);
		mockFhirResourceRetrieval(p);
		
		String conditionSystem = DIABETES_CODESYSTEM;
		String conditionCode = DIABETES_CODE;
		
		ValueSet type2Diabetes = mockValueSetRetrieval("Type2Diabetes", conditionSystem, conditionCode);
		
		Condition c = new Condition();
		c.setId("Type2DiabetesCondition");
		c.setSubject(new Reference(p));
		c.getCode().getCoding().add(new Coding().setSystem(conditionSystem).setCode(conditionCode));
		mockFhirResourceRetrieval(c);
		mockFhirResourceRetrieval("/Condition?code=" + conditionSystem + "%7C" + conditionCode + "&subject=Patient%2F" + p.getId(), makeBundle(c));
		mockFhirResourceRetrieval("/Condition?code=" + type2Diabetes.getId() + "&subject=Patient%2F" + p.getId(), makeBundle(c));

		mockLibraryRetrieval("FHIRHelpers", "1.0.0", "cql/fhir-measure/FHIRHelpers.cql");
		Library library = mockLibraryRetrieval("TestAdultMalesSimple", "1.0.0", "cql/fhir-measure/test-adult-males-valuesets.cql");
		Measure measure = getCohortMeasure(MEASURE_NAME, library, "Numerator");
		mockFhirResourceRetrieval(measure);
		
		mockValueSetRetrieval("MarriedStatus", MARITAL_STATUS_SYSTEM, MARRIED);

		this.clientContext = new FHIRClientContext.Builder()
				.withDefaultClient(client)
				.build();
	}

	@Test
	public void build_onlyClientContext() {
		MeasureEvaluator evaluator = new R4MeasureEvaluatorBuilder()
				.withClientContext(clientContext)
				.build();
		validateMeasureEvaluator(evaluator);
	}

	@Test
	public void build_withCacheContext() throws Exception {
		try(RetrieveCacheContext cacheContext = new DefaultRetrieveCacheContext(new CaffeineConfiguration<>())) {
			MeasureEvaluator evaluator = new R4MeasureEvaluatorBuilder()
					.withClientContext(clientContext)
					.withRetrieveCacheContext(cacheContext)
					.build();
			validateMeasureEvaluator(evaluator);
		}
		
		verify(1, getRequestedFor(urlMatching("/ValueSet/Type2Diabetes/.*")));
		verify(1, getRequestedFor(urlMatching("/Condition\\?code=" + DIABETES_CODESYSTEM + ".*")));
		verify(0, getRequestedFor(urlMatching("/Condition\\?code=Type2Diabetes.*")));
	}
	
	@Test
	public void build_withExpandValueSetsFalse() throws Exception {
		try(RetrieveCacheContext cacheContext = new DefaultRetrieveCacheContext(new CaffeineConfiguration<>())) {
			MeasureEvaluator evaluator = new R4MeasureEvaluatorBuilder()
					.withClientContext(clientContext)
					.withRetrieveCacheContext(cacheContext)
					.withExpandValueSets(false)
					.build();
			validateMeasureEvaluator(evaluator);
		}
		
		verify(0, getRequestedFor(urlMatching("/ValueSet/Type2Diabetes/$expand.*")));
		verify(0, getRequestedFor(urlMatching("/Condition\\?code=" + DIABETES_CODESYSTEM + ".*")));
		verify(1, getRequestedFor(urlMatching("/Condition\\?code=Type2Diabetes.*")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void build_noClientContext() {
		new R4MeasureEvaluatorBuilder().build();
	}

	private void validateMeasureEvaluator(MeasureEvaluator evaluator) {
		MeasureReport report = evaluator.evaluatePatientMeasure(
				MEASURE_NAME,
				PATIENT_ID,
				null,
				new MeasureEvidenceOptions()
		);

		List<MeasureReportGroupComponent> groups = report.getGroup();
		Assert.assertEquals(1, groups.size());

		MeasureReportGroupPopulationComponent component = groups.get(0).getPopulationFirstRep();

		List<Coding> codings = component.getCode().getCoding();
		Assert.assertEquals(1, codings.size());

		Coding coding = codings.get(0);
		Assert.assertEquals("initial-population", coding.getCode());

		int count = component.getCount();
		Assert.assertEquals(1, count);
	}
}
