package com.ibm.cohort.engine.measure;

import java.math.BigDecimal;

import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Type;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverter;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverterFactory;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirVersionEnum;


public class MeasureReportParameterHelper {

	private static final Logger logger = LoggerFactory.getLogger(MeasureReportParameterHelper.class);

	private static final FhirTypeConverter converter = new FhirTypeConverterFactory().create(FhirVersionEnum.R4);

	public static IBaseDatatype getFhirTypeValue(Object value) {
		if (value instanceof Interval) {
			return getFhirTypeForInterval((Interval) value);
		}
		else {
			return getFhirTypeForNonInterval(value);
		}
	}

	protected static IBaseDatatype getFhirTypeForInterval(Interval interval) {
		Object low =  interval.getLow();
		Object high = interval.getHigh();
		
		if (low instanceof DateTime) {
			return converter.toFhirInterval(interval);
		}
		else if (low instanceof BigDecimal) {
			Interval tmpInterval = new Interval(
					new Quantity().withValue((BigDecimal) low),
					interval.getLowClosed(),
					new Quantity().withValue((BigDecimal) high),
					interval.getHighClosed()
			);
			return converter.toFhirInterval(tmpInterval);
		}
		else if (low instanceof Integer) {
			Interval tmpInterval = new Interval(
					new Quantity().withValue(BigDecimal.valueOf((Integer) low)),
					interval.getLowClosed(),
					new Quantity().withValue(BigDecimal.valueOf((Integer) high)),
					interval.getHighClosed()
			);
			return converter.toFhirInterval(tmpInterval);
		}
		else if (low instanceof Quantity) {
			return converter.toFhirRange(interval);
		}
		else if (low instanceof Time) {
			Range range = new Range();
			range.setLow(getDurationFromTime((Time) low));
			range.setHigh(getDurationFromTime((Time) high));

			return range;
		}
		else  {
			logger.warn("Support not implemented for parameters of type {} on a MeasureReport", low.getClass());
			return null;
		}
	}
	
	protected static IBaseDatatype getFhirTypeForNonInterval(Object value) {
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
			return converter.toFhirDateTime((DateTime) value);
		}
		else if (value instanceof Quantity) {
			return converter.toFhirQuantity((Quantity) value);
		}
		else {
			logger.warn("Support not implemented for parameters of type {} on a MeasureReport", value.getClass());
			return null;
		}
	}

	private static Duration getDurationFromTime(Time time) {
		long lowMs = time.getTime().toNanoOfDay() / 1_000_000;

		Duration duration = new Duration();
		duration.setValue(lowMs);
		duration.setCode("ms");
		duration.setSystem("http://hl7.org/fhir/ValueSet/duration-units");

		return duration;
	}
}
