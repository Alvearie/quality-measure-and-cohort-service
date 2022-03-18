/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;
import org.junit.Test;

import com.ibm.cohort.engine.FhirTestBase;
import com.ibm.cohort.engine.cdm.CDMConstants;
import com.ibm.cohort.engine.cqfruler.CDMContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceHelper;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions.DefineReturnOptions;

public class CDMMeasureEvaluationTest extends FhirTestBase {
	@Test
	public void testDefinesOnMeasureReport() {
		MeasureReport report = new MeasureReport();
		
		Map<VersionedIdentifier, Map<String, Object>> expectedResults = setupTestExpectedResultsContext();
		CDMContext defineContext = setupTestDefineContext(expectedResults);
		
		CDMMeasureEvaluation.addDefineEvaluationToReport(report, defineContext, DefineReturnOptions.ALL);
		
		assertEquals(5, report.getExtension().size());
		
		int index = 0;
		for(Entry<VersionedIdentifier, Map<String, Object>> expectedLibraryResults : expectedResults.entrySet()) {
			for(Entry<String, Object> defineResult : expectedLibraryResults.getValue().entrySet()) {
				Extension extension = report.getExtension().get(index++);
				
				assertEquals(MeasureEvidenceHelper.createEvidenceKey(expectedLibraryResults.getKey(), defineResult.getKey()), extension.getExtensionByUrl(CDMConstants.EVIDENCE_TEXT_URL).getValue().primitiveValue());
				
				//hack because Type does not return equals for 2 identical objects :(
				Type returnType = extension.getExtensionByUrl(CDMConstants.EVIDENCE_VALUE_URL).getValue();
				
				if(defineResult.getValue() instanceof Boolean) {
					assertTrue(returnType.isBooleanPrimitive());
					assertEquals(defineResult.getValue(), ((BooleanType)returnType).booleanValue());
				}
				else if(defineResult.getValue() instanceof String) {
					assertTrue(returnType.isPrimitive());
					assertEquals(defineResult.getValue(), returnType.primitiveValue());
				}
				else if(defineResult.getValue() instanceof DomainResource) {
					assertTrue(returnType instanceof Reference);
				}
			}
		}
	}
	
	@Test
	public void testBooleanDefinesOnMeasureReport() {
		MeasureReport report = new MeasureReport();
		CDMContext defineContext = setupTestDefineContext(setupTestExpectedResultsContext());
		
		CDMMeasureEvaluation.addDefineEvaluationToReport(report, defineContext, DefineReturnOptions.BOOLEAN);
		
		assertEquals(3, report.getExtension().size());
		
		for(Extension extension : report.getExtension()) {
			Type returnType = extension.getExtensionByUrl(CDMConstants.EVIDENCE_VALUE_URL).getValue();
			
			assertTrue(returnType instanceof BooleanType);
		}
	}
	
	@Test
	public void testSetReportMeasureToMeasureId__measureFromBundleWithoutMetaVersion__onlyMeasurePortionWithoutHistoryOnReport() {
		MeasureReport report = new MeasureReport();

		String bundleInput = "{\"resourceType\":\"Bundle\",\"id\":\"98765\",\"entry\":[{\"fullUrl\":\"https://full-url-to/fhir-server/api/v4/Measure/id1\",\"resource\":{\"resourceType\":\"Measure\",\"id\":\"id1\"}}]}";

		Measure measure = (Measure) fhirParser.parseResource(Bundle.class, bundleInput).getEntryFirstRep().getResource();

		CDMMeasureEvaluation.setReportMeasureToMeasureId(report, measure);

		assertEquals("Measure/id1", report.getMeasure());
	}

	@Test
	public void testSetReportMeasureToMeasureId__measureFromBundleWithMetaVersion__onlyMeasurePortionWithHistoryOnReport() {
		MeasureReport report = new MeasureReport();

		String bundleInput = "{\"resourceType\":\"Bundle\",\"id\":\"98765\",\"entry\":[{\"fullUrl\":\"https://full-url-to/fhir-server/api/v4/Measure/id1\",\"resource\":{\"resourceType\":\"Measure\",\"id\":\"id1\",\"meta\":{\"versionId\":\"2\"}}}]}";

		Measure measure = (Measure) fhirParser.parseResource(Bundle.class, bundleInput).getEntryFirstRep().getResource();

		CDMMeasureEvaluation.setReportMeasureToMeasureId(report, measure);

		assertEquals("Measure/id1/_history/2", report.getMeasure());
	}

	@Test
	public void testSetReportMeasureToMeasureId__noMetaVersion__noHistoryInMeasureOnReport() {
		MeasureReport report = new MeasureReport();

		String measureInput = "{\"resourceType\":\"Measure\",\"id\":\"id1\"}";

		Measure measure = fhirParser.parseResource(Measure.class, measureInput);

		CDMMeasureEvaluation.setReportMeasureToMeasureId(report, measure);

		assertEquals("Measure/id1", report.getMeasure());
	}

	@Test
	public void testSetReportMeasureToMeasureId__includesMetaVersion__hasHistoryInMeasureOnReport() {
		MeasureReport report = new MeasureReport();

		String measureInput = "{\"resourceType\":\"Measure\",\"id\":\"id1\",\"meta\":{\"versionId\":\"2\"}}";

		Measure measure = fhirParser.parseResource(Measure.class, measureInput);

		CDMMeasureEvaluation.setReportMeasureToMeasureId(report, measure);

		assertEquals("Measure/id1/_history/2", report.getMeasure());
	}

	private CDMContext setupTestDefineContext(Map<VersionedIdentifier, Map<String, Object>> expectedResults) {
		Library library = new Library();
		CDMContext defineContext = new CDMContext(library);
						
		for(Entry<VersionedIdentifier, Map<String, Object>> expectedLibraryResults : expectedResults.entrySet()) {
			for(Entry<String, Object> defineResult : expectedLibraryResults.getValue().entrySet()) {
				defineContext.addExpressionToCache(expectedLibraryResults.getKey(), defineResult.getKey(), defineResult.getValue());
			}
		}
		
		return defineContext;
	}
	
	private Map<VersionedIdentifier, Map<String, Object>> setupTestExpectedResultsContext() {
		VersionedIdentifier libraryId1 = new VersionedIdentifier();
		libraryId1.setId("LibraryName1");
		libraryId1.setVersion(MeasureEvaluatorTest.DEFAULT_VERSION);
		
		VersionedIdentifier libraryId2 = new VersionedIdentifier();
		libraryId2.setId("LibraryName2");
		libraryId2.setVersion(MeasureEvaluatorTest.DEFAULT_VERSION);
		
		String define1 = "Define 1";
		String define2 = "Define 2";
		String define3 = "Define 3";
		
		boolean boolVal1 = true;
		boolean boolVal2 = false;
		String stringVal = "Hello";
		Patient patientRef = new Patient();
		
		Map<VersionedIdentifier, Map<String, Object>> expectedResults = new HashMap<>();
		
		Map<String, Object> library1ExpectedResults = Stream.of(
				  new AbstractMap.SimpleEntry<>(define1, boolVal1), 
				  new AbstractMap.SimpleEntry<>(define2, boolVal2)
		).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Map<String, Object> library2ExpectedResults = Stream.of(
				  new AbstractMap.SimpleEntry<>(define1, patientRef), 
				  new AbstractMap.SimpleEntry<>(define2, stringVal),
				  new AbstractMap.SimpleEntry<>(define3, boolVal1)
		).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		expectedResults.put(libraryId1, library1ExpectedResults);
		expectedResults.put(libraryId2, library2ExpectedResults);
		
		
		return expectedResults;
	}
}
