/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.TimeZone;

import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Period;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverter;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverterFactory;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.Date;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;
import org.opencds.cqf.cql.engine.runtime.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;

/**
 * This class is meant to handle converting CQL types to FHIR representations for use on
 * a MeasureReport. This class may introduce special conversion behaviors intended only
 * for the MeasureReport (example: converting all CQL DateTimes to a dateTime FHIR representation
 * in UTC). Use caution if attempting to use this class to perform conversions for other purposes.
 */
public class CQLToFHIRMeasureReportHelper {

	private static final Logger logger = LoggerFactory.getLogger(CQLToFHIRMeasureReportHelper.class);

	private static final FhirTypeConverter converter = new FhirTypeConverterFactory().create(FhirVersionEnum.R4);

	public static IBaseDatatype getFhirTypeValue(Object value) {
		if (value instanceof Interval) {
			return getFhirTypeForInterval((Interval) value);
		}
		else {
			return getFhirTypeForNonInterval(value);
		}
	}

	private static IBaseDatatype getFhirTypeForInterval(Interval interval) {
		Object low =  interval.getLow();
		Object high = interval.getHigh();

		if (low instanceof DateTime) {
			// Handle DateTime conversion to force UTC timezone
			Period period = new Period();

			period.setStartElement(createDateTimeType((DateTime) low));
			period.setEndElement(createDateTimeType((DateTime) high));
			return period;
		}
		else if (low instanceof Quantity) {
			return converter.toFhirRange(interval);
		}
		else  {
			logger.warn("Support not implemented for Interval parameters of type {} on a MeasureReport", low.getClass());
			return null;
		}
	}
	
	private static IBaseDatatype getFhirTypeForNonInterval(Object value) {
		if(value instanceof String) {
			return converter.toFhirString((String) value);
		}
		else if (value instanceof BigDecimal) {
			return converter.toFhirDecimal((BigDecimal) value);
		}
		else if (value instanceof Integer) {
			return converter.toFhirInteger((Integer) value);
		}
		else if (value instanceof Time) {
			return converter.toFhirTime((Time) value);
		}
		else if (value instanceof Code) {
			return (Coding) converter.toFhirCoding((Code) value);
		}
		else if (value instanceof Boolean) {
			return converter.toFhirBoolean((Boolean) value);
		}
		else if (value instanceof Concept) {
			return converter.toFhirCodeableConcept((Concept) value);
		}
		else if (value instanceof DateTime) {
			return createDateTimeType((DateTime) value);
		}
		else if (value instanceof Quantity) {
			return converter.toFhirQuantity((Quantity) value);
		}
		else if (value instanceof Ratio) {
			return converter.toFhirRatio((Ratio) value);
		}
		else if (value instanceof Date) {
			return converter.toFhirDate((Date) value);
		}
		else {
			logger.warn("Support not implemented for parameters of type {} on a MeasureReport", value.getClass());
			return null;
		}
	}
	
	private static DateTimeType createDateTimeType(DateTime dateTime) {
		return  new DateTimeType(dateTime.toJavaDate(), TemporalPrecisionEnum.MILLI, TimeZone.getTimeZone(ZoneId.of("Z")));
	}
}
