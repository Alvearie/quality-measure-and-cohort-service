package com.ibm.cohort.engine.measure;

import java.math.BigDecimal;

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
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MeasureReportParameterHelper {

	private static final Logger logger = LoggerFactory.getLogger(MeasureReportParameterHelper.class);

	public static Type getFhirTypeValue(Object value) {
		if (value instanceof Interval) {
			return getFhirTypeForInterval((Interval) value);
		}
		else {
			return getFhirTypeForNonInterval(value);
		}
	}

	protected static Type getFhirTypeForInterval(Interval interval) {
		Object low =  interval.getLow();
		Object high = interval.getHigh();
		
		if (low instanceof DateTime) {
			Period period = new Period();
			period.setStartElement(new DateTimeType(low.toString()));
			period.setEndElement(new DateTimeType(high.toString()));
			return period;
		}
		else if (low instanceof BigDecimal) {
			Range range = new Range();
			SimpleQuantity lowQuantity = new SimpleQuantity();
			lowQuantity.setValue((BigDecimal) low);
			range.setLow(lowQuantity);

			SimpleQuantity highQuantity = new SimpleQuantity();
			highQuantity.setValue((BigDecimal) high);
			range.setHigh(highQuantity);

			return range;
		}
		else if (low instanceof Integer) {
			Range range = new Range();
			SimpleQuantity lowQuantity = new SimpleQuantity();
			lowQuantity.setValue((Integer) low);
			range.setLow(lowQuantity);

			SimpleQuantity highQuantity = new SimpleQuantity();
			highQuantity.setValue((Integer) high);
			range.setHigh(highQuantity);

			return range;
		}
		else if (low instanceof Quantity) {
			Range range = new Range();
			
			range.setLow(getFhirQuantityFromCqlQuantity((Quantity) low));
			range.setHigh(getFhirQuantityFromCqlQuantity((Quantity) high));
			
			return range;
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
	
	protected static Type getFhirTypeForNonInterval(Object value) {
		if(value instanceof String) {
			return new StringType((String) value);
		}
		else if (value instanceof BigDecimal) {
			return new DecimalType((BigDecimal) value);
		}
		else if (value instanceof Integer) {
			return new IntegerType((Integer) value);
		}
		else if (value instanceof Time) {
			return new TimeType(value.toString());
		}
		else if (value instanceof Code) {
			return getCodingFromCode((Code) value);
		}
		else if (value instanceof Boolean) {
			return new BooleanType((Boolean) value);
		}
		else if (value instanceof Concept) {
			Concept cqlConcept = (Concept) value;
			
			CodeableConcept codeableConcept = new CodeableConcept();
			codeableConcept.setText(cqlConcept.getDisplay());
			
			for(Code code : cqlConcept.getCodes()) {
				codeableConcept.addCoding(getCodingFromCode(code));
			}
			
			return codeableConcept;
		}
		else if (value instanceof DateTime) {
			return new DateTimeType(value.toString());
		}
		else if (value instanceof Quantity) {
			return getFhirQuantityFromCqlQuantity((Quantity) value);
		}
		else {
			logger.warn("Support not implemented for parameters of type {} on a MeasureReport", value.getClass());
			return null;
		}
	}
	
	private static Coding getCodingFromCode(Code code) {
		Coding coding = new Coding(code.getSystem(), code.getCode(), code.getDisplay());
		coding.setVersion(code.getVersion());
		return coding;
	}
	
	private static org.hl7.fhir.r4.model.Quantity getFhirQuantityFromCqlQuantity(Quantity cqlQuantity) {
		org.hl7.fhir.r4.model.Quantity fhirQuantity = new org.hl7.fhir.r4.model.Quantity(cqlQuantity.getValue().doubleValue());
		fhirQuantity.setUnit(cqlQuantity.getUnit());
		return fhirQuantity;
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
