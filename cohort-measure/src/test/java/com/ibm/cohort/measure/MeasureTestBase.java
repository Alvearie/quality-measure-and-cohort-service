/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ibm.cohort.cql.cdm.CDMConstants;
import com.ibm.cohort.cql.hapi.FhirTestBase;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.codesystems.MeasureScoring;
import org.junit.Before;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;

import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureTestBase extends FhirTestBase {
	
	public static final String NUMERATOR_EXCLUSION = "Numerator Exclusion";

	public static final String NUMERATOR = "Numerator";

	public static final String DENOMINATOR_EXCEPTION = "Denominator Exception";

	public static final String DENOMINATOR_EXCLUSION = "Denominator Exclusion";

	public static final String DENOMINATOR = "Denominator";

	public static final String INITIAL_POPULATION = "Initial Population";

	public static final String MEASURE_META_VERSION_ID = "5";

	protected Map<MeasurePopulationType, String> expressionsByPopulationType;
	protected Map<MeasurePopulationType, Integer> expectationsByPopulationType;
	
	protected IGenericClient client = null;

	@Before
	public void setUp() {
		FhirServerConfig config = getFhirServerConfig();
		FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
		FhirClientBuilder builder = factory.newFhirClientBuilder(fhirContext);
		client = builder.createFhirClient(config);

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

	protected Library mockLibraryRetrieval(String libraryName, String libraryVersion, String... cqlResource) throws Exception {
		Library library = getLibrary(libraryName, libraryVersion, cqlResource);
		mockFhirResourceRetrieval(library);

		Bundle bundle = getBundle(library);
		
		String url = URLEncoder.encode( library.getUrl(), StandardCharsets.UTF_8.toString() );
		if( library.getVersion() != null ) {
			url += "&version=" + library.getVersion();
		}
		
		mockFhirResourceRetrieval("/Library?url=" + url + "&_format=json", bundle );
		mockFhirResourceRetrieval("/Library?name%3Aexact=" + library.getName() + "&_format=json", bundle);
		mockFhirResourceRetrieval("/Library?name%3Aexact=" + library.getName() + "&version=" + library.getVersion() + "&_format=json", bundle);
		return library;
	}
	
	protected Measure mockMeasureRetrieval(Measure measure) throws Exception {
		mockFhirResourceRetrieval(measure);
		
		Bundle bundle = getBundle(measure);
		
		String url = URLEncoder.encode( measure.getUrl(), StandardCharsets.UTF_8.toString() );
		
		mockFhirResourceRetrieval("/Measure?url=" + url + "&_format=json", bundle );
		mockFhirResourceRetrieval("/Measure?url=" + url + "&version=" + measure.getVersion() + "&_format=json", bundle );
		mockFhirResourceRetrieval("/Measure?name=" + measure.getName() + "&_sort=-date&_format=json", bundle);
		mockFhirResourceRetrieval("/Measure?name=" + measure.getName() + "&version=" + measure.getVersion() + "&_sort=-date&_format=json", bundle);

		return measure;
	}

	protected Bundle getBundle(Resource... resources) {
		Bundle bundle = new Bundle();
		if( resources != null && resources.length > 0 ) {
			bundle.setId(UUID.randomUUID().toString());
			bundle.setType(Bundle.BundleType.SEARCHSET);
			bundle.setTotal( resources != null ? resources.length : 0 );
			if( resources != null ) {
				for (Resource resource : resources) {
					bundle.getEntry().add(new Bundle.BundleEntryComponent().setResource(resource));
				}
			}
		}
		return bundle;
	}

	public Measure getCohortMeasure(String measureName, Library library, String expression) throws Exception {
		Measure measure = getTemplateMeasure(measureName, library, MeasureScoring.COHORT);

		Measure.MeasureGroupComponent group = new Measure.MeasureGroupComponent();
		addPopulations(group, Collections.singletonMap(MeasurePopulationType.INITIALPOPULATION, expression));
		measure.addGroup(group);

		return measure;
	}

	public Measure getProportionMeasure(String measureName, Library library,
			Map<MeasurePopulationType, String> expressionsByPopType) throws Exception {
		Measure measure = getTemplateMeasure(measureName, library, MeasureScoring.PROPORTION);

		Measure.MeasureGroupComponent group = new Measure.MeasureGroupComponent();
		addPopulations(group, expressionsByPopType);
		measure.addGroup(group);

		return measure;
	}

	public Measure getCareGapMeasure(String measureName, Library library,
			Map<MeasurePopulationType, String> expressionsByPopType, String... careGapExpressions) throws Exception {
		Measure measure = getProportionMeasure(measureName, library, expressionsByPopType);

		assertNotNull(careGapExpressions);
		for (String expression : careGapExpressions) {
			Measure.MeasureGroupPopulationComponent pop = new Measure.MeasureGroupPopulationComponent();
			pop.setId(expression);
			pop.setCode(new CodeableConcept(new Coding(CDMConstants.CDM_CODE_SYSTEM_MEASURE_POPULATION_TYPE,
					CDMConstants.CARE_GAP, "Care Gap")));
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
	
	protected RelatedArtifact asRelation(Library library) {
		return new RelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.DEPENDSON).setResource( library.getUrl() + "|" + library.getVersion() );
	}

	public Measure getTemplateMeasure(String measureName, Library library, MeasureScoring scoring) throws Exception{
		Measure measure = new Measure();
		measure.setId(measureName);
		measure.setName(measureName);
		measure.setVersion("1.0.0");
		measure.setUrl("http://ibm.com/health/Measure/" + URLEncoder.encode(measureName, "UTF-8"));
		measure.setDate(new Date());
		measure.setLibrary(Arrays.asList(asCanonical(library)));
		measure.setScoring(new CodeableConcept().addCoding(new Coding().setCode(scoring.toCode())));
		measure.setMeta(new Meta().setVersionId(MEASURE_META_VERSION_ID));
		return measure;
	}
}
