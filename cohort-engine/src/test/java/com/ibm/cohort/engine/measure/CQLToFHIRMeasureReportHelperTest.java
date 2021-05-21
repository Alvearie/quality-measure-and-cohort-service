package com.ibm.cohort.engine.measure;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.CqlList;
import org.opencds.cqf.cql.engine.runtime.Date;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;
import org.opencds.cqf.cql.engine.runtime.Time;

public class CQLToFHIRMeasureReportHelperTest {

	@Test
	public void testUnsupportedType_shouldReturnNull() {
		assertNull(CQLToFHIRMeasureReportHelper.getFhirTypeValue(new CqlList()));
	}

	@Test
	public void testIntervalDateTime_shouldReturnPeriodInUTC() {
		DateTime startDateTime = new DateTime(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 10, 5, 0), ZoneOffset.of("Z")));
		DateTime endDateTime = new DateTime(OffsetDateTime.of(LocalDateTime.of(2020, 2, 15, 15, 10, 0, 0), ZoneOffset.of("+01:00")));

		Interval interval = new Interval(startDateTime, true, endDateTime, true);

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(interval);

		// Expect output expressed in UTC
		DateTimeType expectedStart = new DateTimeType("2020-01-01T00:10:05+00:00");
		DateTimeType expectedEnd = new DateTimeType("2020-02-15T14:10:00+00:00");

		assertTrue(fhirTypeValue instanceof Period);
		Period castResult = (Period) fhirTypeValue;
		assertEquals(expectedStart.toHumanDisplay(), castResult.getStartElement().toHumanDisplay());
		assertEquals(expectedEnd.toHumanDisplay(), castResult.getEndElement().toHumanDisplay());
	}

	@Test
	public void testIntervalQuantity_shouldReturnRange() {
		String unit = "ml";
		BigDecimal startAmount = new BigDecimal("4.0");
		BigDecimal endAmount = new BigDecimal("5.0");
		Quantity startQuantity = new Quantity().withUnit(unit).withValue(startAmount);
		Quantity endQuantity = new Quantity().withUnit(unit).withValue(endAmount);

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(new Interval(startQuantity, true, endQuantity, true));

		assertTrue(fhirTypeValue instanceof Range);
		Range castResult = (Range) fhirTypeValue;
		verifyBaseTypeAsQuantity(castResult.getLow(), startAmount, unit);
		verifyBaseTypeAsQuantity(castResult.getHigh(), endAmount, unit);
	}

	/*
	 *  Currently only supporting CQL intervals of type DateTime or Quantity.
	 *  Support for intervals of other types may be added in the future.
	 */
	@Test
	public void testIntervalTime_shouldReturnNull() {
		String startTimeString = "00:10:00.000";
		String endTimeString = "00:15:00.000";

		Time startTime = new Time(startTimeString);
		Time endTime = new Time(endTimeString);

		Interval interval = new Interval(startTime, true, endTime, true);
		assertNull(CQLToFHIRMeasureReportHelper.getFhirTypeValue(interval));
	}

	@Test
	public void testIntervalDecimal_shouldReturnNull() {
		Interval interval = new Interval(new BigDecimal("1.4"), true, new BigDecimal("1.5"), true);
		assertNull(CQLToFHIRMeasureReportHelper.getFhirTypeValue(interval));
	}

	@Test
	public void testIntervalInteger_shouldReturnNull() {
		Interval interval = new Interval(1, true, 10, true);
		assertNull(CQLToFHIRMeasureReportHelper.getFhirTypeValue(interval));
	}

	@Test
	public void testIntervalDate_shouldReturnNull() {
		Interval interval = new Interval(new Date("2020-01-02"), true, new Date("2020-06-04"), true);
		assertNull(CQLToFHIRMeasureReportHelper.getFhirTypeValue(interval));
	}

	@Test
	public void testIntegerType() {
		Integer expected = 1;

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof IntegerType);
		assertEquals(expected, ((IntegerType) fhirTypeValue).getValue());
	}

	@Test
	public void testDecimalType() {
		BigDecimal expected = BigDecimal.valueOf(2.3);

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof DecimalType);
		assertEquals(expected, ((DecimalType) fhirTypeValue).getValue());
	}

	@Test
	public void testString() {
		String expected = "strval";

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof StringType);
		assertEquals(expected, ((StringType) fhirTypeValue).getValue());
	}

	@Test
	public void testBoolean() {
		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(false);

		assertTrue(fhirTypeValue instanceof BooleanType);
		assertFalse(((BooleanType) fhirTypeValue).getValue());
	}

	@Test
	public void testDateTime_returnsSameDateTimeInUTC() {
		String inputDateTimeString = "2020-01-02T00:00:00.000-04:00";

		DateTime expected = new DateTime(inputDateTimeString, OffsetDateTime.now().getOffset());

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof DateTimeType);
		assertEquals("2020-01-02T04:00:00.000+00:00", ((DateTimeType) fhirTypeValue).getValueAsString());
	}

	@Test
	public void testDate() {
		String dateString = "2020-01-02";

		Date expected = new Date(dateString);

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof DateType);
		assertEquals(dateString, ((DateType) fhirTypeValue).getValueAsString());
	}

	@Test
	public void testTime() {
		String timeString = "00:10:00.000";

		Time expected = new Time(timeString);

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof TimeType);
		assertEquals(timeString, ((TimeType) fhirTypeValue).getValueAsString());
	}

	@Test
	public void testQuantity() {
		String unit = "ml";
		BigDecimal amount = new BigDecimal("4.0");
		Quantity quantity = new Quantity().withUnit(unit).withValue(amount);

		verifyBaseTypeAsQuantity(CQLToFHIRMeasureReportHelper.getFhirTypeValue(quantity), amount, unit);
	}

	@Test
	public void testRatio() {
		String unit = "ml";

		BigDecimal amount1 = new BigDecimal("4.0");
		Quantity quantity1 = new Quantity().withUnit(unit).withValue(amount1);

		BigDecimal amount2 = new BigDecimal("7.0");
		Quantity quantity2 = new Quantity().withUnit(unit).withValue(amount2);

		Ratio ratio = new Ratio().setNumerator(quantity1).setDenominator(quantity2);

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(ratio);

		assertTrue(fhirTypeValue instanceof org.hl7.fhir.r4.model.Ratio);
		org.hl7.fhir.r4.model.Ratio castResult = (org.hl7.fhir.r4.model.Ratio) fhirTypeValue;
		verifyBaseTypeAsQuantity(castResult.getNumerator(), amount1, unit);
		verifyBaseTypeAsQuantity(castResult.getDenominator(), amount2, unit);
	}

	private void verifyBaseTypeAsQuantity(IBaseDatatype baseDatatype, BigDecimal amount, String unit) {
		assertTrue(baseDatatype instanceof org.hl7.fhir.r4.model.Quantity);

		org.hl7.fhir.r4.model.Quantity quantity = (org.hl7.fhir.r4.model.Quantity) baseDatatype;

		assertEquals(unit, quantity.getCode());
		assertEquals(amount, quantity.getValue());
	}

	@Test
	public void testCode() {
		String codeString = "code";
		String system = "system";
		String display = "display";
		String version = "version";

		Code code = new Code().withCode(codeString).withSystem(system).withDisplay(display).withVersion(version);

		verifyBaseTypeAsCode(CQLToFHIRMeasureReportHelper.getFhirTypeValue(code), codeString, system, display, version);
	}

	@Test
	public void testConcept() {
		String codeString1 = "code1";
		String system1 = "system1";
		String display1 = "display1";
		String version1 = "version1";

		String codeString2 = "code2";
		String system2 = "system2";
		String display2 = "display2";
		String version2 = "version2";

		String conceptDisplay = "conceptDisplay";

		Code code1 = new Code().withCode(codeString1).withSystem(system1).withDisplay(display1).withVersion(version1);
		Code code2 = new Code().withCode(codeString2).withSystem(system2).withDisplay(display2).withVersion(version2);

		Concept concept = new Concept().withCodes(Arrays.asList(code1, code2)).withDisplay(conceptDisplay);

		IBaseDatatype fhirTypeValue = CQLToFHIRMeasureReportHelper.getFhirTypeValue(concept);
		assertTrue(fhirTypeValue instanceof CodeableConcept);
		CodeableConcept castResult = (CodeableConcept) fhirTypeValue;

		List<Coding> codingList = castResult.getCoding();

		boolean code1Found = false;
		boolean code2Found = false;
		for (Coding coding : codingList) {
			if (coding.getCode().equals(codeString1)) {
				verifyBaseTypeAsCode(coding, codeString1, system1, display1, version1);
				code1Found = true;
			}
			else if (coding.getCode().equals(codeString2)) {
				verifyBaseTypeAsCode(coding, codeString2, system2, display2, version2);
				code2Found = true;
			}
			else {
				Assert.fail();
			}
		}

		assertTrue(code1Found && code2Found);
		assertEquals(2, codingList.size());
	}

	private void verifyBaseTypeAsCode(IBaseDatatype baseDatatype, String code, String system, String display, String version) {
		assertTrue(baseDatatype instanceof Coding);

		Coding coding = (Coding) baseDatatype;

		assertEquals(code, coding.getCode());
		assertEquals(system, coding.getSystem());
		assertEquals(display, coding.getDisplay());
		assertEquals(version, coding.getVersion());
	}
}