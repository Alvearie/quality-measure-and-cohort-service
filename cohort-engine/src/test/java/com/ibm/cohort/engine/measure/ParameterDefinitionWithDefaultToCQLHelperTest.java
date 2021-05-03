package com.ibm.cohort.engine.measure;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.ZoneOffset;

import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.ParameterDefinition;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Date;
import org.opencds.cqf.cql.engine.runtime.DateTime;

import com.ibm.cohort.engine.cdm.CDMConstants;

public class ParameterDefinitionWithDefaultToCQLHelperTest {

	@Test
	public void testBase64Binary__shouldReturnString() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("base64Binary");
		String base64String = "AAA";
		Base64BinaryType value = new Base64BinaryType(base64String);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, value);

		assertEquals(base64String, ParameterDefinitionWithDefaultToCQLHelper.getCqlObject(parameterDefinition));
	}

	@Test
	public void testBoolean__shouldReturnBoolean() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("boolean");
		Boolean expectedBoolean = true;
		BooleanType value = new BooleanType(expectedBoolean);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, value);

		assertEquals(expectedBoolean, ParameterDefinitionWithDefaultToCQLHelper.getCqlObject(parameterDefinition));
	}

	@Test
	public void testDate__shouldReturnDate() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("date");
		String dateString = "2020-01-01";

		DateType value = new DateType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, value);

		Date expectedDate = new Date(dateString);

		assertTrue(expectedDate.equal(ParameterDefinitionWithDefaultToCQLHelper.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testDateTimeNoTimezone__shouldReturnDateTimeInUTC() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("dateTime");
		String dateString = "2020-01-01T00:00:00.0";

		DateTimeType value = new DateTimeType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, value);

		DateTime expectedDateTime = new DateTime("2020-01-01T00:00:00.0", ZoneOffset.UTC);

		assertTrue(expectedDateTime.equal(ParameterDefinitionWithDefaultToCQLHelper.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testDateTimeWithTimezone__shouldReturnDateTimeInCorrectTimezone() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("dateTime");
		String dateString = "2020-01-01T00:00:00.0+04:00";

		DateTimeType value = new DateTimeType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, value);

		DateTime expectedDateTime = new DateTime("2020-01-01T00:00:00.0", ZoneOffset.ofHours(4));

		assertTrue(expectedDateTime.equal(ParameterDefinitionWithDefaultToCQLHelper.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testDecimal__shouldReturnDecimal() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("decimal");

		BigDecimal bigDecimalValue = new BigDecimal(1.5);
		DecimalType value = new DecimalType(bigDecimalValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, value);

		assertEquals(bigDecimalValue, ParameterDefinitionWithDefaultToCQLHelper.getCqlObject(parameterDefinition));
	}

	private ParameterDefinition getBaseParameterDefinition(String type) {
		ParameterDefinition parameterDefinition = new ParameterDefinition();
		parameterDefinition.setName(type + "Param");
		parameterDefinition.setUse(ParameterDefinition.ParameterUse.IN);
		parameterDefinition.setType(type);

		return parameterDefinition;
	}
}