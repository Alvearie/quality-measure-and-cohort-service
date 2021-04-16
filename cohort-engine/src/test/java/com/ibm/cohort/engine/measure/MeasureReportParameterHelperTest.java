package com.ibm.cohort.engine.measure;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.CqlList;
import org.opencds.cqf.cql.engine.runtime.Date;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;
import org.opencds.cqf.cql.engine.runtime.Time;

public class MeasureReportParameterHelperTest {
	
	@Test
	public void testUnsupportedType_shouldReturnNull() {
		assertNull(MeasureReportParameterHelper.getFhirTypeValue(new CqlList()));
	}
	
	@Test
	public void testIntegerType() {
		Integer expected = 1;
		
		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(expected);
		
		assertTrue(fhirTypeValue instanceof IntegerType);
		assertEquals(expected, ((IntegerType) fhirTypeValue).getValue());
	}

	@Test
	public void testDecimalType() {
		BigDecimal expected = BigDecimal.valueOf(2.3);

		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(expected);
		
		assertTrue(fhirTypeValue instanceof DecimalType);
		assertEquals(expected, ((DecimalType) fhirTypeValue).getValue());
	}
	
	@Test
	public void testString() {
		String expected = "strval";

		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof StringType);
		assertEquals(expected, ((StringType) fhirTypeValue).getValue());
	}
	
	@Test
	public void testBoolean() {
		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(false);

		assertTrue(fhirTypeValue instanceof BooleanType);
		assertFalse(((BooleanType) fhirTypeValue).getValue());
	}

	@Test
	public void testDateTime() {
		String dateTimeString = "2020-01-02T00:00:00.000";
		
		DateTime expected = new DateTime(dateTimeString, OffsetDateTime.now().getOffset());
		
		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof DateTimeType);
		assertEquals(dateTimeString, ((DateTimeType) fhirTypeValue).getValueAsString());
	}

	@Test
	public void testDate() {
		String dateString = "2020-01-02";

		Date expected = new Date(dateString);

		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof DateType);
		assertEquals(dateString, ((DateType) fhirTypeValue).getValueAsString());
	}

	@Test
	public void testTime() {
		String timeString = "00:10:00.000";

		Time expected = new Time(timeString);

		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(expected);

		assertTrue(fhirTypeValue instanceof TimeType);
		assertEquals(timeString, ((TimeType) fhirTypeValue).getValueAsString());
	}
	
	@Test
	public void testQuantity() {
		String unit = "ml";
		BigDecimal amount = new BigDecimal("4.0");
		Quantity quantity = new Quantity().withUnit(unit).withValue(amount);
		
		verifyBaseTypeAsQuantity(MeasureReportParameterHelper.getFhirTypeValue(quantity), amount, unit);
	}

	@Test
	public void testRatio() {
		String unit = "ml";
		
		BigDecimal amount1 = new BigDecimal("4.0");
		Quantity quantity1 = new Quantity().withUnit(unit).withValue(amount1);

		BigDecimal amount2 = new BigDecimal("7.0");
		Quantity quantity2 = new Quantity().withUnit(unit).withValue(amount2);

		Ratio ratio = new Ratio().setNumerator(quantity1).setDenominator(quantity2);

		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(ratio);

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
		
		verifyBaseTypeAsCode(MeasureReportParameterHelper.getFhirTypeValue(code), codeString, system, display, version);
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

		IBaseDatatype fhirTypeValue = MeasureReportParameterHelper.getFhirTypeValue(concept);
		assertTrue(fhirTypeValue instanceof CodeableConcept);
		CodeableConcept castResult = (CodeableConcept) fhirTypeValue;

		List<Coding> codingList = castResult.getCoding();
		int codingCount = 0;
		for (Coding coding : codingList) {
			if (coding.getCode().equals(codeString1)) {
				verifyBaseTypeAsCode(coding, codeString1, system1, display1, version1);
				codingCount++;
			}
			else if (coding.getCode().equals(codeString2)) {
				verifyBaseTypeAsCode(coding, codeString2, system2, display2, version2);
				codingCount++;
			}
			else {
				Assert.fail();
			}
		}
		
		assertEquals(2, codingCount);
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