/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;


import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.ParameterDefinition;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.UriType;
import org.junit.Test;

import com.ibm.cohort.engine.cdm.CDMConstants;
import com.ibm.cohort.engine.measure.parameter.UnsupportedFhirTypeException;
import com.ibm.cohort.cql.evaluation.parameters.BooleanParameter;
import com.ibm.cohort.cql.evaluation.parameters.CodeParameter;
import com.ibm.cohort.cql.evaluation.parameters.ConceptParameter;
import com.ibm.cohort.cql.evaluation.parameters.DateParameter;
import com.ibm.cohort.cql.evaluation.parameters.DatetimeParameter;
import com.ibm.cohort.cql.evaluation.parameters.DecimalParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntegerParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntervalParameter;
import com.ibm.cohort.cql.evaluation.parameters.QuantityParameter;
import com.ibm.cohort.cql.evaluation.parameters.RatioParameter;
import com.ibm.cohort.cql.evaluation.parameters.StringParameter;
import com.ibm.cohort.cql.evaluation.parameters.TimeParameter;

public class R4ParameterDefinitionWithDefaultToCohortParameterConverterTest {

	@Test
	public void testBase64Binary__shouldReturnStringParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("base64Binary");
		String base64String = "AAA";
		Base64BinaryType fhirValue = new Base64BinaryType(base64String);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new StringParameter(base64String), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testBoolean__shouldReturnBooleanParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("boolean");
		BooleanType fhirValue = new BooleanType(true);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new BooleanParameter(true), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testDate__shouldReturnDate() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("date");
		String dateString = "2020-01-01";

		DateType fhirValue = new DateType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new DateParameter(dateString), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testDateTimeNoTimezone__shouldReturnDatetimeParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("dateTime");
		String dateString = "2020-01-01T00:00:00.0";

		DateTimeType fhirValue = new DateTimeType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new DatetimeParameter("2020-01-01T00:00:00.0"), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testDateTimeWithTimezone__shouldReturnDatetimeParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("dateTime");
		String dateString = "2020-01-01T00:00:00.0+04:00";

		DateTimeType fhirValue = new DateTimeType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new DatetimeParameter(dateString), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testDecimal__shouldReturnDecimalParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("decimal");

		String decimalString = "1.5";
		BigDecimal bigDecimalValue = new BigDecimal(decimalString);
		DecimalType fhirValue = new DecimalType(bigDecimalValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new DecimalParameter(decimalString), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testInstant__shouldReturnDateTimeParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("instant");

		String instantString = "2020-01-01T12:30:00.0Z";
		InstantType fhirValue = new InstantType(instantString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new DatetimeParameter(instantString), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testInteger__shouldReturnIntegerParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("integer");

		int expectedValue = 10;
		IntegerType fhirValue = new IntegerType(expectedValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new IntegerParameter(expectedValue), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testString__shouldReturnStringParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("string");

		String expectedValue = "data";
		StringType fhirValue = new StringType(expectedValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new StringParameter(expectedValue), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testTime__shouldReturnTimeParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("time");

		String timeString = "12:30:00";
		TimeType fhirValue = new TimeType(timeString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new TimeParameter(timeString), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testUri__shouldReturnStringParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("uri");

		String uriString = "a-b-c-d-e-f-g";
		UriType fhirValue = new UriType(uriString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(new StringParameter(uriString), R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test(expected = UnsupportedFhirTypeException.class)
	public void testAnnotation__shouldThrowException() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Annotation");

		Annotation fhirValue = new Annotation();

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition);
	}

	@Test(expected = UnsupportedFhirTypeException.class)
	public void testAttachment__shouldThrowException() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Attachment");

		Attachment fhirValue = new Attachment();

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition);
	}

	@Test
	public void testCoding__shouldReturnCodeParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Coding");

		Coding fhirValue = makeCoding("sys", "val", "dis", "ver");

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		CodeParameter expectedParameter = new CodeParameter().setSystem("sys").setValue("val").setDisplay("dis").setVersion("ver");

		assertEquals(expectedParameter, R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testCodeableConcept__shouldReturnConceptParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("CodeableConcept");

		CodeableConcept fhirValue = new CodeableConcept();
		fhirValue.setText("plainText");

		fhirValue.addCoding(makeCoding("s1", "val1", "d1", "ver1"));
		fhirValue.addCoding(makeCoding("s2", "val2", "d2", "ver2"));

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		List<CodeParameter> expectedCodeParameters = new ArrayList<>();
		expectedCodeParameters.add(new CodeParameter().setSystem("s1").setValue("val1").setDisplay("d1").setVersion("ver1"));
		expectedCodeParameters.add(new CodeParameter().setSystem("s2").setValue("val2").setDisplay("d2").setVersion("ver2"));

		ConceptParameter expectedParameter = new ConceptParameter().setDisplay("plainText");
		expectedParameter.setCodes(expectedCodeParameters);

		assertEquals(expectedParameter, R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testPeriod__shouldReturnIntervalParameterOfDateTimeParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Period");

		Period fhirValue = new Period();
		fhirValue.setStartElement(new DateTimeType("2020-01-01T12:00:00.0"));
		fhirValue.setEndElement(new DateTimeType("2020-02-04T11:00:00.0-05:00"));

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		IntervalParameter expectedParameter = new IntervalParameter(
				new DatetimeParameter("2020-01-01T12:00:00.0"), true,
				new DatetimeParameter("2020-02-04T11:00:00.0-05:00"), true);

		assertEquals(expectedParameter, R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testQuantity__shouldReturnQuantityParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Quantity");

		String decimalString = "1.5";
		BigDecimal bigDecimalValue = new BigDecimal(decimalString);
		String unit = "ml";

		org.hl7.fhir.r4.model.Quantity fhirValue = new org.hl7.fhir.r4.model.Quantity();
		fhirValue.setUnit(unit);
		fhirValue.setValue(bigDecimalValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		QuantityParameter expectedParameter = new QuantityParameter()
				.setUnit(unit)
				.setAmount(decimalString);

		assertEquals(expectedParameter, R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testRange__shouldReturnIntervalParameterOfQuantityParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Range");

		String lowString = "1.5";
		String highString = "2.5";
		BigDecimal lowValue = new BigDecimal(lowString);
		BigDecimal highValue = new BigDecimal(highString);
		String unit = "ml";

		Range fhirValue = new Range()
				.setLow(new org.hl7.fhir.r4.model.Quantity().setValue(lowValue).setUnit(unit))
				.setHigh(new org.hl7.fhir.r4.model.Quantity().setValue(highValue).setUnit(unit));

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		IntervalParameter expectedParameter = new IntervalParameter(new QuantityParameter().setUnit(unit).setAmount(lowString),
																   true,
																   new QuantityParameter().setUnit(unit).setAmount(highString),
																   true);

		assertEquals(expectedParameter, R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	@Test
	public void testRatio__shouldReturnRatioParameter() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Ratio");

		String denominatorString = "1.5";
		String numeratorString = "2.5";
		BigDecimal denominatorValue = new BigDecimal(denominatorString);
		BigDecimal numeratorValue = new BigDecimal(numeratorString);
		String unit = "ml";

		org.hl7.fhir.r4.model.Ratio fhirValue = new org.hl7.fhir.r4.model.Ratio()
				.setDenominator(new org.hl7.fhir.r4.model.Quantity().setValue(denominatorValue).setUnit(unit))
				.setNumerator(new org.hl7.fhir.r4.model.Quantity().setValue(numeratorValue).setUnit(unit));

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		RatioParameter expectedParameter = new RatioParameter()
				.setDenominator(new QuantityParameter().setUnit(unit).setAmount(denominatorString))
				.setNumerator(new QuantityParameter().setUnit(unit).setAmount(numeratorString));

		assertEquals(expectedParameter, R4ParameterDefinitionWithDefaultToCohortParameterConverter.toCohortParameter(parameterDefinition));
	}

	private ParameterDefinition getBaseParameterDefinition(String type) {
		ParameterDefinition parameterDefinition = new ParameterDefinition();
		parameterDefinition.setName(type + "Param");
		parameterDefinition.setUse(ParameterDefinition.ParameterUse.IN);
		parameterDefinition.setType(type);

		return parameterDefinition;
	}

	private Coding makeCoding(String system, String value, String display, String version) {
		Coding coding = new Coding();

		coding.setSystem(system);
		coding.setCode(value);
		coding.setDisplay(display);
		coding.setVersion(version);

		return coding;
	}
}