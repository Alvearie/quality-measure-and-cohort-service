package com.ibm.cohort.engine.api.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParameterTest {
	@Test
	public void when_serialize_deserialize___properties_remain_the_same() throws Exception {
		Parameter integer = new IntegerParameter("MyInteger", 10);
		Parameter decimal = new DecimalParameter("MyDecimal", "+100.99e10");
		Parameter bool = new BooleanParameter("MyDecimal", true);
		Parameter str = new StringParameter("MyDecimal", "StringValueHere");
		Parameter date = new DateParameter("MyDate", "2020-07-04");
		Parameter datetime = new DatetimeParameter("MyDatetime", "2020-07-04T23:00:00");
		Parameter time = new TimeParameter("MyTime", "@T23:00:00");

		QuantityParameter quantity = new QuantityParameter("MyQuantity", "10", "mg/mL");
		QuantityParameter denominator = new QuantityParameter("denominator", "100", "mg/mL");
		Parameter ratio = new RatioParameter("MyRatio", quantity, denominator);
		Parameter end = new IntegerParameter("end", 1000);
		Parameter interval = new IntervalParameter("MyInterval", integer, true, end, false);
		CodeParameter code = new CodeParameter("MyCode", "http://hl7.org/terminology/blob", "1234", "Blob", "1.0.0");
		Parameter concept = new ConceptParameter("MyConcept", "MyConcept", code);
		
		List<Parameter> parameters = Arrays.asList(integer, decimal, bool, str, date, datetime, time, quantity, denominator, ratio, interval, code, concept);
		
		ObjectMapper mapper = new ObjectMapper();
		String serialized = mapper.writeValueAsString(parameters);
		assertFalse( serialized.contains("com.ibm") );
		
		List<Parameter> deserialized = mapper.readValue(serialized, new TypeReference<List<Parameter>>(){});
		assertEquals( parameters.size(), deserialized.size() );
		
		for( int i=0; i<deserialized.size(); i++ ) {
			Parameter expected = parameters.get(i);
			Parameter actual = parameters.get(i);
			
			assertEquals( expected, actual );
		}
		
		for( Parameter param : deserialized ) {
			assertNotNull( param.toCqlType() );
		}
	}
}
