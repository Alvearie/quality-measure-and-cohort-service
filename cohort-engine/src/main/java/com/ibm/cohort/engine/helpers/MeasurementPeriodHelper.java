package com.ibm.cohort.engine.helpers;

import java.time.ZoneOffset;

import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Precision;

public class MeasurementPeriodHelper {

	/**
	 * Return a org.opencds.cqf.cql.engine.runtime.DateTime object given a
	 * FHIR date String as input. Match the specification for periodStart
	 * in the <a href=
	 * "https://www.hl7.org/fhir/measure-operation-evaluate-measure.html">FHIR
	 * evaluate operation</a>.
	 *
	 * If a DateTime string is passed in, only the date portion of the string
	 * is used during the conversion.
	 *
	 * @param periodStart     FHIR date string representing the start of the
	 *                        Measurement Period.
	 * @return DateTime representing the input string at the earliest point in
	 * time represented by the string.
	 *
	 */
	public static DateTime getPeriodStart(String periodStart) {
		return getPeriodStart(periodStart, ZoneOffset.UTC);
	}

	public static DateTime getPeriodStart(String periodStart, ZoneOffset zoneOffset) {
		DateTime dateTime = new DateTime(trimInputToDate(periodStart), zoneOffset);

		if (dateTime.getPrecision().equals(Precision.YEAR)) {
			dateTime = dateTime.expandPartialMin(Precision.MONTH);
		}
		if (dateTime.getPrecision().equals(Precision.MONTH)) {
			dateTime = dateTime.expandPartialMin(Precision.DAY);
		}

		return dateTime
				.expandPartialMin(Precision.HOUR)
				.expandPartialMin(Precision.MINUTE)
				.expandPartialMin(Precision.SECOND)
				.expandPartialMin(Precision.MILLISECOND);
	}

	/**
	 * Return a org.opencds.cqf.cql.engine.runtime.DateTime object given a
	 * FHIR date String as input. Match the specification for periodEnd
	 * in the <a href=
	 * "https://www.hl7.org/fhir/measure-operation-evaluate-measure.html">FHIR
	 * evaluate operation</a>.
	 *
	 * If a DateTime string is passed in, only the date portion of the string
	 * is used during the conversion.
	 *
	 * @param periodEnd       FHIR date string representing the start of the
	 *                        Measurement Period.
	 * @return DateTime representing the input string at the latest point in
	 * time represented by the string.
	 *
	 */
	public static DateTime getPeriodEnd(String periodEnd) {
		return getPeriodEnd(periodEnd, ZoneOffset.UTC);
	}

	public static DateTime getPeriodEnd(String periodEnd, ZoneOffset zoneOffset) {
		DateTime dateTime = new DateTime(trimInputToDate(periodEnd), zoneOffset);

		if (dateTime.getPrecision().equals(Precision.YEAR)) {
			dateTime = dateTime.expandPartialMax(Precision.MONTH);
		}
		if (dateTime.getPrecision().equals(Precision.MONTH)) {
			dateTime = dateTime.expandPartialMax(Precision.DAY);
		}

		return dateTime
				.expandPartialMax(Precision.HOUR)
				.expandPartialMax(Precision.MINUTE)
				.expandPartialMax(Precision.SECOND)
				.expandPartialMax(Precision.MILLISECOND);
	}

	private static String trimInputToDate(String inputString) {
		int indexOfT = inputString.indexOf('T');

		if (indexOfT < 0) {
			return inputString;
		} else {
			return inputString.substring(0, indexOfT);
		}
	}
}
