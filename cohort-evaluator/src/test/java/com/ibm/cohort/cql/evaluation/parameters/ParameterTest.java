/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParameterTest {
	protected static Validator validator = null;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		// See https://openliberty.io/guides/bean-validation.html
		//TODO: The validator below is recommended to be injected using CDI in the guide
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}
	
	@Test
	public void when_serialize_deserialize___properties_remain_the_same() throws Exception {
		Parameter integer = new IntegerParameter(10);
		Parameter decimal = new DecimalParameter("+100.99e10");
		Parameter bool = new BooleanParameter(true);
		Parameter str = new StringParameter("StringValueHere");
		Parameter date = new DateParameter("2020-07-04");
		Parameter datetime = new DatetimeParameter("2020-07-04T23:00:00-05:00");
		Parameter time = new TimeParameter("@T23:00:00");

		QuantityParameter quantity = new QuantityParameter("10", "mg/mL");
		QuantityParameter denominator = new QuantityParameter("100", "mg/mL");
		Parameter ratio = new RatioParameter(quantity, denominator);
		Parameter end = new IntegerParameter(1000);
		Parameter interval = new IntervalParameter(integer, true, end, false);
		CodeParameter code = new CodeParameter("http://hl7.org/terminology/blob", "1234", "Blob", "1.0.0");
		Parameter concept = new ConceptParameter("MyConcept", code);
		
		List<Parameter> parameters = Arrays.asList(integer, decimal, bool, str, date, datetime, time, quantity, denominator, ratio, interval, code, concept);
		
		ObjectMapper mapper = new ObjectMapper();
		String serialized = mapper.writeValueAsString(parameters);
		System.out.println(serialized);
		assertFalse( serialized.contains("com.ibm") );
		
		List<Parameter> deserialized = mapper.readValue(serialized, new TypeReference<List<Parameter>>(){});
		assertEquals( parameters.size(), deserialized.size() );
		
		for( int i=0; i<deserialized.size(); i++ ) {
			Parameter expected = parameters.get(i);
			Parameter actual = deserialized.get(i);
			
			assertEquals( expected, actual );
		}
		
		for( Parameter param : deserialized ) {
			assertNotNull( param.toCqlType() );
		}
	}
	
	@Test
	public void datetime_interval_with_non_inclusive_end___ends_just_before_value() {
		IntervalParameter parameter = new IntervalParameter(
				new DatetimeParameter("2020-03-14T00:00:00.0-05:00"),
				true,
				new DatetimeParameter("2021-03-14T00:00:00.0-05:00"),
				false
				);
		
		Interval interval = (Interval) parameter.toCqlType();
		assertEquals("2021-03-13T23:59:59.999", interval.getEnd().toString());
	}

	@Test
	public void datetime_with_no_time_or_timezone___defaults_to_midnight_UTC() {
		DatetimeParameter parameter = new DatetimeParameter("2020-01-01");

		DateTime dateTime = (DateTime) parameter.toCqlType();

		DateTime expectedDateTime = new DateTime(OffsetDateTime.of(LocalDate.of(2020, 1,1), LocalTime.MIN, ZoneOffset.UTC));
		assertEquals(ZoneOffset.UTC, dateTime.getDateTime().getOffset());
		assertEquals(expectedDateTime.toJavaDate(), dateTime.toJavaDate());
	}

	@Test
	public void datetime_with_no_timezone___defaults_to_UTC() {
		DatetimeParameter parameter = new DatetimeParameter("2020-01-01T00:00:00.0");

		DateTime dateTime = (DateTime) parameter.toCqlType();

		DateTime expectedDateTime = new DateTime(OffsetDateTime.of(LocalDate.of(2020, 1,1), LocalTime.MIN, ZoneOffset.UTC));
		assertEquals(ZoneOffset.UTC, dateTime.getDateTime().getOffset());
		assertEquals(expectedDateTime.toJavaDate(), dateTime.toJavaDate());
	}
	
	@Test
	public void boolean_parameter_no_value___fails_validation() {
		BooleanParameter param = new BooleanParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void boolean_parameter_with_value___passes_validation() {
		BooleanParameter param = new BooleanParameter(false);
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void code_parameter_no_value___fails_validation() {
		CodeParameter param = new CodeParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void code_parameter_with_value___passes_validation() {
		CodeParameter param = new CodeParameter();
		param.setValue("initial-population");
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void concept_parameter_null_codes___fails_validation() {
		ConceptParameter param = new ConceptParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void concept_parameter_empty_codes___fails_validation() {
		ConceptParameter param = new ConceptParameter("dislay", Collections.emptyList());
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void concept_parameter_with_codes___passes_validation() {
		CodeParameter code = new CodeParameter("system", "value", "display", "version");
		ConceptParameter param = new ConceptParameter("myconcept", Arrays.asList(code));
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void date_parameter_no_value___fails_validation() {
		DateParameter param = new DateParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void date_parameter_with_value___passes_validation() {
		DateParameter param = new DateParameter("@2020-10-10");
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void datetime_parameter_no_value___fails_validation() {
		DatetimeParameter param = new DatetimeParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void datetime_parameter_with_value___passes_validation() {
		DatetimeParameter param = new DatetimeParameter("@2020-10-10T00:00:00.0-05:00");
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void decimal_parameter_no_value___fails_validation() {
		DecimalParameter param = new DecimalParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void decimal_parameter_with_value___passes_validation() {
		DecimalParameter param = new DecimalParameter("1.09e-123");
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void integer_parameter_no_value___fails_validation() {
		IntegerParameter param = new IntegerParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void integer_parameter_with_value___passes_validation() {
		IntegerParameter param = new IntegerParameter(10);
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void interval_parameter_no_value___fails_validation() {
		IntervalParameter param = new IntervalParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(2, violations.size());
	}
	
	@Test
	public void interval_parameter_with_value___passes_validation() {
		IntervalParameter param = new IntervalParameter(new IntegerParameter(10), true, new IntegerParameter(100), false);
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void quantity_parameter_no_value___fails_validation() {
		QuantityParameter param = new QuantityParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(2, violations.size());
	}
	
	@Test
	public void quantity_parameter_with_value___passes_validation() {
		QuantityParameter param = new QuantityParameter("10", "mg/mL");
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void ratio_parameter_no_value___fails_validation() {
		RatioParameter param = new RatioParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(2, violations.size());
	}
	
	@Test
	public void ratio_parameter_with_value___passes_validation() {
		QuantityParameter numerator = new QuantityParameter("10", "mg/mL");
		QuantityParameter denominator = new QuantityParameter("100", "mg/mL");
		
		RatioParameter param = new RatioParameter(numerator, denominator);
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void string_parameter_no_value___fails_validation() {
		StringParameter param = new StringParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void string_parameter_with_value___passes_validation() {
		StringParameter param = new StringParameter("mystring");
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
	
	@Test
	public void time_parameter_no_value___fails_validation() {
		StringParameter param = new StringParameter();
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(1, violations.size());
	}
	
	@Test
	public void time_parameter_with_value___passes_validation() {
		StringParameter param = new StringParameter("T20:30:40.500");
		Set<ConstraintViolation<Parameter>> violations = validator.validate(param);
		assertEquals(0, violations.size());
	}
}
