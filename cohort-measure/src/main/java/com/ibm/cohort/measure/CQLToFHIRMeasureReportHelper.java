/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

//import org.hl7.fhir.instance.model.api.IBaseDatatype;
//import org.hl7.fhir.r4.model.Coding;
//import org.hl7.fhir.r4.model.DateTimeType;
//import org.hl7.fhir.r4.model.Period;
import com.ibm.cohort.measure.wrapper.BaseWrapper;
import com.ibm.cohort.measure.wrapper.WrapperFactory;
import com.ibm.cohort.measure.wrapper.element.CodeableConceptWrapper;
import com.ibm.cohort.measure.wrapper.element.CodingWrapper;
import com.ibm.cohort.measure.wrapper.element.PeriodWrapper;
import com.ibm.cohort.measure.wrapper.element.QuantityWrapper;
import com.ibm.cohort.measure.wrapper.element.RangeWrapper;
import com.ibm.cohort.measure.wrapper.element.RatioWrapper;
import com.ibm.cohort.measure.wrapper.type.BooleanWrapper;
import com.ibm.cohort.measure.wrapper.type.CodeWrapper;
import com.ibm.cohort.measure.wrapper.type.DateTimeWrapper;
import com.ibm.cohort.measure.wrapper.type.DateWrapper;
import com.ibm.cohort.measure.wrapper.type.DecimalWrapper;
import com.ibm.cohort.measure.wrapper.type.IntegerWrapper;
import com.ibm.cohort.measure.wrapper.type.StringWrapper;
import com.ibm.cohort.measure.wrapper.type.TimeWrapper;
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

/**
 * This class is meant to handle converting CQL types to FHIR representations for use on
 * a MeasureReport. This class may introduce special conversion behaviors intended only
 * for the MeasureReport (example: converting all CQL DateTimes to a dateTime FHIR representation
 * in UTC). Use caution if attempting to use this class to perform conversions for other purposes.
 */
public class CQLToFHIRMeasureReportHelper {

	private static final Logger logger = LoggerFactory.getLogger(CQLToFHIRMeasureReportHelper.class);

//	private static final FhirTypeConverter converter = new FhirTypeConverterFactory().create(FhirVersionEnum.R4);

	private final WrapperFactory wrapperFactory;

	public CQLToFHIRMeasureReportHelper(WrapperFactory wrapperFactory) {
		this.wrapperFactory = wrapperFactory;
	}

	public BaseWrapper getFhirTypeValue(Object value) {
		if (value instanceof Interval) {
			return getFhirTypeForInterval((Interval) value);
		}
		else {
			return getFhirTypeForNonInterval(value);
		}
	}

	private BaseWrapper getFhirTypeForInterval(Interval interval) {
		Object low =  interval.getLow();
		Object high = interval.getHigh();

		if (low instanceof DateTime) {
			// Handle DateTime conversion to force UTC timezone
			PeriodWrapper period = wrapperFactory.newPeriod();

			period.setStart(low.toString());
			period.setEnd(high.toString());
			return period;
		}
		else if (low instanceof Quantity) {
			RangeWrapper range = wrapperFactory.newRange();

			range.setLow(convertQuantity((Quantity) low));
			range.setHigh(convertQuantity((Quantity) high));

			return range;
		}
		else  {
			logger.warn("Support not implemented for Interval parameters of type {} on a MeasureReport", low.getClass());
			return null;
		}
	}
	
	private BaseWrapper getFhirTypeForNonInterval(Object value) {
		if(value instanceof String) {
			StringWrapper string = wrapperFactory.newString();
			string.setValue((String) value);
			return string;
		}
		else if (value instanceof BigDecimal) {
			DecimalWrapper decimal = wrapperFactory.newDecimal();
			decimal.setValue((BigDecimal) value);
			return decimal;
		}
		else if (value instanceof Integer) {
			IntegerWrapper integer = wrapperFactory.newInteger();
			integer.setValue((Integer) value);
			return integer;
		}
		else if (value instanceof Time) {
			TimeWrapper time = wrapperFactory.newTime();
			time.setValue(((Time)value).getTime());
			return time;
		}
		else if (value instanceof Code) {
			Code code = (Code)value;
			return convertCode(code);
		}
		else if (value instanceof Boolean) {
			BooleanWrapper bool = wrapperFactory.newBoolean();
			bool.setValue((Boolean)value);
			return bool;
		}
		else if (value instanceof Concept) {
			Concept concept = (Concept)value;
			CodeableConceptWrapper codeableConcept = wrapperFactory.newCodeableConcept();
			codeableConcept.setText(concept.getDisplay());
			if (concept.getCodes() != null) {
				for (Code c : concept.getCodes()) {
					codeableConcept.addCoding(convertCode(c));
				}
			}
			return codeableConcept;
		}
		else if (value instanceof DateTime) {
			DateTimeWrapper dateTime = wrapperFactory.newDateTime();
			dateTime.setValue(value.toString());
			return dateTime;
		}
		else if (value instanceof Quantity) {
			Quantity quantity = (Quantity)value;
			return convertQuantity(quantity);
		}
		else if (value instanceof Ratio) {
			Ratio cqlRatio = (Ratio)value;
			RatioWrapper ratio = wrapperFactory.newRatio();
			ratio.setNumerator(convertQuantity(cqlRatio.getNumerator()));
			ratio.setDenominator(convertQuantity(cqlRatio.getDenominator()));
			return ratio;
		}
		else if (value instanceof Date) {
			DateWrapper date = wrapperFactory.newDate();

			return converter.toFhirDate((Date) value);
		}
		else {
			logger.warn("Support not implemented for parameters of type {} on a MeasureReport", value.getClass());
			return null;
		}
	}
	
//	private ZonedDateTime createZonedDateTime(DateTime dateTime) {
//		return dateTime.getDateTime().toZonedDateTime();
//	}

	private CodingWrapper convertCode(Code code) {
		CodingWrapper coding = wrapperFactory.newCoding();
		coding.setSystem(code.getSystem());
		coding.setCode(code.getCode());
		coding.setDisplay(code.getDisplay());
		coding.setVersion(code.getVersion());
		return coding;
	}

	private QuantityWrapper convertQuantity(Quantity quantity) {
		QuantityWrapper retVal = wrapperFactory.newQuantity();

		// KWAS TODO: This is copied from the DBCG R4 converter...do we care?
		retVal.setSystem("http://unitsofmeasure.org");

		retVal.setCode(quantity.getUnit());
		retVal.setValue(quantity.getValue());

		return retVal;
	}
}
