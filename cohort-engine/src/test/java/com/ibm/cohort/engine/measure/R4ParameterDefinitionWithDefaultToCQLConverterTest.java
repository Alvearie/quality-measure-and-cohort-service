package com.ibm.cohort.engine.measure;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.ZoneOffset;
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
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.Date;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;
import org.opencds.cqf.cql.engine.runtime.Time;

import com.ibm.cohort.engine.cdm.CDMConstants;
import com.ibm.cohort.engine.measure.parameter.UnsupportedFhirTypeException;

public class R4ParameterDefinitionWithDefaultToCQLConverterTest {

	@Test
	public void testBase64Binary__shouldReturnString() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("base64Binary");
		String base64String = "AAA";
		Base64BinaryType fhirValue = new Base64BinaryType(base64String);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(base64String, R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition));
	}

	@Test
	public void testBoolean__shouldReturnBoolean() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("boolean");
		BooleanType fhirValue = new BooleanType(true);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(true, R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition));
	}

	@Test
	public void testDate__shouldReturnDate() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("date");
		String dateString = "2020-01-01";

		DateType fhirValue = new DateType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		Date expectedDate = new Date(dateString);

		assertTrue(expectedDate.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testDateTimeNoTimezone__shouldReturnDateTimeInUTC() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("dateTime");
		String dateString = "2020-01-01T00:00:00.0";

		DateTimeType fhirValue = new DateTimeType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		DateTime expectedDateTime = new DateTime("2020-01-01T00:00:00.0", ZoneOffset.UTC);

		assertTrue(expectedDateTime.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testDateTimeWithTimezone__shouldReturnDateTimeInCorrectTimezone() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("dateTime");
		String dateString = "2020-01-01T00:00:00.0+04:00";

		DateTimeType fhirValue = new DateTimeType(dateString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		DateTime expectedDateTime = new DateTime("2020-01-01T00:00:00.0", ZoneOffset.ofHours(4));

		assertTrue(expectedDateTime.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testDecimal__shouldReturnDecimal() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("decimal");

		BigDecimal bigDecimalValue = new BigDecimal(1.5);
		DecimalType fhirValue = new DecimalType(bigDecimalValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(bigDecimalValue, R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition));
	}

	@Test
	public void testInstant__shouldReturnDateTime() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("instant");

		InstantType fhirValue = new InstantType("2020-01-01T12:30:00.0Z");

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertTrue(new DateTime("2020-01-01T12:30:00.0", ZoneOffset.UTC).equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testInteger__shouldReturnInteger() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("integer");

		int expectedValue = 10;
		IntegerType fhirValue = new IntegerType(expectedValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(expectedValue, R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition));
	}

	@Test
	public void testString__shouldReturnString() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("string");

		String expectedValue = "data";
		StringType fhirValue = new StringType(expectedValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(expectedValue, R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition));
	}

	@Test
	public void testTime__shouldReturnTime() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("time");

		String timeString = "12:30:00";
		TimeType fhirValue = new TimeType(timeString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		Time expectedValue = new Time(timeString);

		assertTrue(expectedValue.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testUri__shouldReturnString() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("uri");

		String uriString = "a-b-c-d-e-f-g";
		UriType fhirValue = new UriType(uriString);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		assertEquals(uriString, R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition));
	}

	@Test(expected = UnsupportedFhirTypeException.class)
	public void testAnnotation__shouldThrowException() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Annotation");

		Annotation fhirValue = new Annotation();

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition);
	}

	@Test(expected = UnsupportedFhirTypeException.class)
	public void testAttachment__shouldThrowException() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Attachment");

		Attachment fhirValue = new Attachment();

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition);
	}

	@Test
	public void testCoding__shouldReturnCode() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Coding");

		Coding fhirValue = makeCoding("sys", "val", "dis", "ver");

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		Code expectedCode = new Code().withSystem("sys").withCode("val").withDisplay("dis").withVersion("ver");

		assertTrue(expectedCode.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testEmptyCoding__shouldReturnEmptyCode() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Coding");

		Coding fhirValue = new Coding();

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		Code expectedCode = new Code();

		assertTrue(expectedCode.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testCodeableConcept__shouldReturnConcept() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("CodeableConcept");

		CodeableConcept fhirValue = new CodeableConcept();
		fhirValue.setText("plainText");

		fhirValue.addCoding(makeCoding("s1", "val1", "d1", "ver1"));
		fhirValue.addCoding(makeCoding("s2", "val2", "d2", "ver2"));

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		List<Code> expectedCodes = new ArrayList<>();
		expectedCodes.add(new Code().withSystem("s1").withCode("val1").withDisplay("d1").withVersion("ver1"));
		expectedCodes.add(new Code().withSystem("s2").withCode("val2").withDisplay("d2").withVersion("ver2"));

		Concept expectedConcept = new Concept().withDisplay("plainText")
				.withCodes(expectedCodes);

		assertTrue(expectedConcept.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testPeriod__shouldReturnIntervalOfDateTime() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Period");

		Period fhirValue = new Period();
		fhirValue.setStartElement(new DateTimeType("2020-01-01T12:00:00.0"));
		fhirValue.setEndElement(new DateTimeType("2020-02-04T11:00:00.0-05:00"));

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		Interval expectedInterval = new Interval(new DateTime("2020-01-01T12:00:00.0", ZoneOffset.UTC), true,
												 new DateTime("2020-02-04T11:00:00.0-05:00", ZoneOffset.UTC), true);

		assertTrue(expectedInterval.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testQuantity__shouldReturnQuantity() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Quantity");

		BigDecimal bigDecimalValue = new BigDecimal(1.5);
		String unit = "ml";

		org.hl7.fhir.r4.model.Quantity fhirValue = new org.hl7.fhir.r4.model.Quantity();
		fhirValue.setUnit(unit);
		fhirValue.setValue(bigDecimalValue);

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		Quantity expectedQuantity = new Quantity()
				.withUnit(unit)
				.withValue(bigDecimalValue);

		assertTrue(expectedQuantity.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testRange__shouldReturnIntervalOfQuantity() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Range");

		BigDecimal lowValue = new BigDecimal(1.5);
		BigDecimal highValue = new BigDecimal(2.5);
		String unit = "ml";

		Range fhirValue = new Range()
				.setLow(new org.hl7.fhir.r4.model.Quantity().setValue(lowValue).setUnit(unit))
				.setHigh(new org.hl7.fhir.r4.model.Quantity().setValue(highValue).setUnit(unit));

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		Interval expectedInterval = new Interval(new Quantity().withUnit(unit).withValue(lowValue),
												 true,
												 new Quantity().withUnit(unit).withValue(highValue),
												 true);

		assertTrue(expectedInterval.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
	}

	@Test
	public void testRatio__shouldReturnRatio() {
		ParameterDefinition parameterDefinition = getBaseParameterDefinition("Ratio");

		BigDecimal denominatorValue = new BigDecimal(1.5);
		BigDecimal numeratorValue = new BigDecimal(2.5);
		String unit = "ml";

		org.hl7.fhir.r4.model.Ratio fhirValue = new org.hl7.fhir.r4.model.Ratio()
				.setDenominator(new org.hl7.fhir.r4.model.Quantity().setValue(denominatorValue).setUnit(unit))
				.setNumerator(new org.hl7.fhir.r4.model.Quantity().setValue(numeratorValue).setUnit(unit));

		parameterDefinition.addExtension(CDMConstants.PARAMETER_DEFAULT_URL, fhirValue);

		Ratio expectedInterval = new Ratio()
				.setDenominator(new Quantity().withUnit(unit).withValue(denominatorValue))
				.setNumerator(new Quantity().withUnit(unit).withValue(numeratorValue));

		assertTrue(expectedInterval.equal(R4ParameterDefinitionWithDefaultToCQLConverter.getCqlObject(parameterDefinition)));
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