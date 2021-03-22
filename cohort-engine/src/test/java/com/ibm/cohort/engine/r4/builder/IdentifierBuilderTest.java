/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.r4.builder;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;

public class IdentifierBuilderTest {

	@Test
	public void testBuilder() {
		String system = "TEST SYSTEM";
		String code = "TEST CODE";
		String display = "TEST DISPLAY";
		String value = "TEST VALUE";
		
		Coding coding = new Coding(system, code, display);
		CodeableConcept type = new CodeableConcept(coding);
		
		Period period = new Period();
		period.setStart(new Date());
		period.setEnd(new Date());
		
		Reference assigner = new Reference("TEST REFERENCE");
		
		Identifier identifier = new IdentifierBuilder()
				.buildUse(IdentifierUse.OFFICIAL)
				.buildSystem(system)
				.buildType(type)
				.buildPeriod(period)
				.buildValue(value)
				.buildAssigner(assigner)
				.build();
		
		assertEquals(IdentifierUse.OFFICIAL, identifier.getUse());
		assertEquals(system, identifier.getSystem());
		assertEquals(type, identifier.getType());
		assertEquals(period, identifier.getPeriod());
		assertEquals(value, identifier.getValue());
		assertEquals(assigner, identifier.getAssigner());
	}
	
	@Test
	public void testStringUse() {
		Identifier identifier = new IdentifierBuilder()
				.buildUse("official")
				.build();
		
		assertEquals(IdentifierUse.OFFICIAL, identifier.getUse());
	}
	
	@Test(expected = FHIRException.class)
	public void testInvalidStringUse() {
		new IdentifierBuilder()
				.buildUse("super fake identifier use")
				.build();
	}
}
