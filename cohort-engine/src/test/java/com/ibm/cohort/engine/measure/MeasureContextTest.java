/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.cohort.engine.parameter.IntervalParameter;
import com.ibm.cohort.engine.parameter.Parameter;

public class MeasureContextTest {
	protected static Validator validator = null;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		// See https://openliberty.io/guides/bean-validation.html
		//TODO: The validator below is recommended to be injected using CDI in the guide
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}
	
	@Test
	public void when_multiple_forms_of_id___fails_validation() {
		MeasureContext context = new MeasureContext("measureId", Collections.emptyMap(),
				new Identifier().setSystem("system").setValue("value"));
		
		Set<ConstraintViolation<MeasureContext>> violations = validator.validate(context);
		printViolations(violations);
		assertEquals(1, violations.size());
	}

	@Test
	public void when_only_measureId___passes_validation() {
		MeasureContext context = new MeasureContext("measureId", Collections.emptyMap());
		
		Set<ConstraintViolation<MeasureContext>> violations = validator.validate(context);
		printViolations(violations);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void when_only_identifier___passes_validation() {
		MeasureContext context = new MeasureContext(null, Collections.emptyMap(), new Identifier().setSystem("system").setValue("value"));
		
		Set<ConstraintViolation<MeasureContext>> violations = validator.validate(context);
		printViolations(violations);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void when_version_without_identifier___fails_validation() {
		MeasureContext context = new MeasureContext("measureId", Collections.emptyMap(), null, "version");
		
		Set<ConstraintViolation<MeasureContext>> violations = validator.validate(context);
		printViolations(violations);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void when_invalid_parameters___fails_validation() {
		Map<String, Parameter> parameters = new HashMap<>();
		parameters.put("Measurement Period", new IntervalParameter());
		
		MeasureContext context = new MeasureContext("measureId", parameters);
		Set<ConstraintViolation<MeasureContext>> violations = validator.validate(context);
		printViolations(violations);
		assertEquals(2, violations.size());
	}
	
	private void printViolations(Set<ConstraintViolation<MeasureContext>> violations) {
		for( ConstraintViolation<MeasureContext> violation : violations ) {
			System.out.print(violation.getPropertyPath().toString());
			System.out.print(": ");
			System.out.println(violation.getMessage());
		}
	}
	
}
