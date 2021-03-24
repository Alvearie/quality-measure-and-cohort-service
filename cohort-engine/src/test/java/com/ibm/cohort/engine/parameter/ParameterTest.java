/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParameterTest {
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
				new DatetimeParameter("2020-03-14T00:00:00-05:00"),
				true,
				new DatetimeParameter("2021-03-14T00:00:00-05:00"),
				false
				);
		
		Interval interval = (Interval) parameter.toCqlType();
		assertEquals("2021-03-13T23:59:59.999", interval.getEnd().toString());
	}
}
