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
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;
import org.junit.Test;

import com.ibm.cohort.engine.cqfruler.CDMContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceHelper;

public class CDMMeasureEvaluationTest {
	@Test
	public void testDefinesOnMeasureReport() {
		MeasureReport report = new MeasureReport();
		Library library = new Library();
		CDMContext defineContext = new CDMContext(library);
		
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
		
		for(Entry<VersionedIdentifier, Map<String, Object>> expectedLibraryResults : expectedResults.entrySet()) {
			for(Entry<String, Object> defineResult : expectedLibraryResults.getValue().entrySet()) {
				defineContext.addExpressionToCache(expectedLibraryResults.getKey(), defineResult.getKey(), defineResult.getValue());
			}
		}
		
		CDMMeasureEvaluation.addDefineEvaluationToReport(report, defineContext);
		
		assertEquals(5, report.getExtension().size());
		
		int index = 0;
		for(Entry<VersionedIdentifier, Map<String, Object>> expectedLibraryResults : expectedResults.entrySet()) {
			for(Entry<String, Object> defineResult : expectedLibraryResults.getValue().entrySet()) {
				Extension extension = report.getExtension().get(index++);
				
				assertEquals(MeasureEvidenceHelper.createEvidenceKey(expectedLibraryResults.getKey(), defineResult.getKey()), extension.getExtensionByUrl(CDMMeasureEvaluation.EVIDENCE_TEXT_URL).getValue().primitiveValue());
				
				//hack because Type does not return equals for 2 identical objects :(
				Type returnType = extension.getExtensionByUrl(CDMMeasureEvaluation.EVIDENCE_VALUE_URL).getValue();
				
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
	
}
