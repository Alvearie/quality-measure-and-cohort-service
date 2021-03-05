/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.evidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.junit.Test;

import com.ibm.cohort.engine.measure.MeasureEvaluatorTest;

public class MeasureEvidenceHelperTest {
	
	private static final Map<Object, Class<?>> EXPECTED_CONVERSIONS = Stream.of(
					  new AbstractMap.SimpleEntry<>("idea", StringType.class), 
					  new AbstractMap.SimpleEntry<>(Boolean.FALSE, BooleanType.class),
					  new AbstractMap.SimpleEntry<>(3.5, DecimalType.class),
					  new AbstractMap.SimpleEntry<>(new Date(), DateTimeType.class),
					  new AbstractMap.SimpleEntry<>(3, IntegerType.class),
					  new AbstractMap.SimpleEntry<>(new Patient(), Reference.class),
					  new AbstractMap.SimpleEntry<>(new CodeableConcept(), CodeableConcept.class),
					  new AbstractMap.SimpleEntry<>(new Annotation(), Annotation.class),
					  new AbstractMap.SimpleEntry<>(new Attachment(), Attachment.class),
					  new AbstractMap.SimpleEntry<>(new Period(), Period.class),
					  new AbstractMap.SimpleEntry<>(new RelatedArtifact(), RelatedArtifact.class),
					  new AbstractMap.SimpleEntry<>(new Quantity(), Quantity.class)
			).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	
	@Test
	public void testFhirType() {
		for(Entry<Object, Class<?>> entry : EXPECTED_CONVERSIONS.entrySet()) {
			assertEquals(entry.getValue(), MeasureEvidenceHelper.getFhirType(entry.getKey()).getClass());
		}
	}
	
	@Test
	public void testFhirTypes() {
		Set<Object> objects = EXPECTED_CONVERSIONS.keySet();
		
		List<Type> types = MeasureEvidenceHelper.getFhirTypes(objects);
		
		assertEquals(EXPECTED_CONVERSIONS.values().size(), types.size());
		assertTrue(EXPECTED_CONVERSIONS.values().containsAll(types.stream().map(x -> x.getClass()).collect(Collectors.toSet())));		
	}
	
	@Test
	public void testMeasureEvidenceKey() {
		String libraryName1 = "LibraryName1";
		String libraryName2 = "LibraryName2";
		
		VersionedIdentifier libraryId1 = new VersionedIdentifier();
		libraryId1.setId(libraryName1);
		libraryId1.setVersion(MeasureEvaluatorTest.DEFAULT_VERSION);
		
		VersionedIdentifier libraryId2 = new VersionedIdentifier();
		libraryId2.setId(libraryName2);
		libraryId2.setVersion(MeasureEvaluatorTest.DEFAULT_VERSION);
		
		String define1 = "define1";
		String define2 = "define2";
		
		assertEquals(libraryName1 + "_" + MeasureEvaluatorTest.DEFAULT_VERSION + "." + define1, MeasureEvidenceHelper.createEvidenceKey(libraryId1, define1));
		assertEquals(libraryName1 + "_" + MeasureEvaluatorTest.DEFAULT_VERSION + "." + define2, MeasureEvidenceHelper.createEvidenceKey(libraryId1, define2));
		assertEquals(libraryName2 + "_" + MeasureEvaluatorTest.DEFAULT_VERSION + "." + define1, MeasureEvidenceHelper.createEvidenceKey(libraryId2, define1));
		assertEquals(libraryName2 + "_" + MeasureEvaluatorTest.DEFAULT_VERSION + "." + define2, MeasureEvidenceHelper.createEvidenceKey(libraryId2, define2));
	}
}
