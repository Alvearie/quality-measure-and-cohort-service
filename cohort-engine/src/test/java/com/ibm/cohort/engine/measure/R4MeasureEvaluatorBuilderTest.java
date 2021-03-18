/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.measure.cache.DefaultRetrieveCacheContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupPopulationComponent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class R4MeasureEvaluatorBuilderTest extends BaseMeasureTest {

	private static final String PATIENT_ID = "FHIRClientContextTest-PatientId";
	private static final String MEASURE_NAME = "FHIRClientContextTest-MeasureName";

	private FHIRClientContext clientContext;

	@Before
	public void setup() throws Exception {
		super.setUp();

		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		mockPatientRetrieval(PATIENT_ID, AdministrativeGender.MALE, 22);

		Library library = mockLibraryRetrieval("TestAdultMales", "1.0.0", "cql/fhir-measure/test-adult-males.cql");
		Measure measure = getCohortMeasure(MEASURE_NAME, library, "Numerator");
		mockFhirResourceRetrieval(measure);

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
