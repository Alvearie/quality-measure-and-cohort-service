/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import com.ibm.cohort.engine.cqfruler.DefineContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceHelper;

public class CDMMeasureEvaluationTest {
	@Test
	public void addDefines() {
		MeasureReport report = new MeasureReport();
		Library library = new Library();
		DefineContext defineContext = new DefineContext(library);
		
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
		
		defineContext.addExpressionToCache(libraryId1, define1, boolVal1);
		defineContext.addExpressionToCache(libraryId1, define2, boolVal2);
		defineContext.addExpressionToCache(libraryId2, define1, patientRef);
		defineContext.addExpressionToCache(libraryId2, define2, stringVal);
		defineContext.addExpressionToCache(libraryId2, define3, boolVal1);
		
		CDMMeasureEvaluation.addDefineEvaluationToReport(report, defineContext);
		
		assertEquals(5, report.getExtension().size());
		
		for(Extension extension : report.getExtension()) {
			assertEquals(MeasureEvidenceHelper.createEvidenceKey(libraryId1, define1), extension.getExtensionByUrl(CDMMeasureEvaluation.EVIDENCE_TEXT_URL).getValue().primitiveValue());
			
//			System.out.println(extension.getExtensionByUrl(CDMMeasureEvaluation.EVIDENCE_TEXT_URL).getValue());
//			List<Extension> values = extension.getExtensionsByUrl(CDMMeasureEvaluation.EVIDENCE_VALUE_URL);
			
		}
	}
	
}
