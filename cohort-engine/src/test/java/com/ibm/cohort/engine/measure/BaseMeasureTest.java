/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.codesystems.MeasureScoring;
import org.junit.Before;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;

import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.engine.FhirClientFactory;
import com.ibm.cohort.engine.FhirServerConfig;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class BaseMeasureTest extends BaseFhirTest {
	
	public static final String NUMERATOR_EXCLUSION = "Numerator Exclusion";

	public static final String NUMERATOR = "Numerator";

	public static final String DENOMINATOR_EXCEPTION = "Denominator Exception";

	public static final String DENOMINATOR_EXCLUSION = "Denominator Exclusion";

	public static final String DENOMINATOR = "Denominator";

	public static final String INITIAL_POPULATION = "Initial Population";

	protected Map<MeasurePopulationType, String> expressionsByPopulationType;
	protected Map<MeasurePopulationType, Integer> expectationsByPopulationType;
	
	IGenericClient client = null;

	@Before
	public void setUp() {
		FhirServerConfig config = getFhirServerConfig();
		client = FhirClientFactory.newInstance(fhirContext).createFhirClient(config);

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
	
	protected List<MeasureReport.MeasureReportGroupPopulationComponent> verifyStandardPopulationCounts(
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

	protected void addPopulations(Measure.MeasureGroupComponent group,
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
