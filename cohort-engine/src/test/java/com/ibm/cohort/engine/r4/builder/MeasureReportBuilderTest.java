/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.r4.builder;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportStatus;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportType;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;

public class MeasureReportBuilderTest {
	@Test
	public void testBuilder() {
		String measureRef = "MEASURE REF";
		String patientRef = "PATIENT REF";
		MeasureReportStatus status = MeasureReportStatus.ERROR;
		MeasureReportType type = MeasureReportType.INDIVIDUAL;
		
		Date startDate = new Date();
		Date endDate = new Date();
		
		Interval interval = new Interval(DateTime.fromJavaDate(startDate), true,
										DateTime.fromJavaDate(endDate), true);
		
		MeasureReport report = new MeasureReportBuilder()
				.buildMeasureReference(measureRef)
				.buildPatientReference(patientRef)
				.buildPeriod(interval)
				.buildStatus(status.toCode())
				.buildType(type)
				.build();
		
		assertEquals(measureRef, report.getMeasure());
		assertEquals(patientRef, report.getSubject().getReference());
		assertEquals(startDate, report.getPeriod().getStart());
		assertEquals(endDate, report.getPeriod().getEnd());
		assertEquals(status, report.getStatus());
		assertEquals(type, report.getType());
	}
	
	@Test
	public void testValidTypeString() {
		MeasureReport report = new MeasureReportBuilder()
				.buildType(MeasureReportType.INDIVIDUAL.toCode())
				.build();
		
		assertEquals(MeasureReportType.INDIVIDUAL, report.getType());
	}
	
	@Test(expected = FHIRException.class)
	public void testInvalidTypeString() {
		new MeasureReportBuilder()
				.buildType("fake type")
				.build();
	}
	
	@Test
	public void testInvalidStatus() {
		MeasureReport report = new MeasureReportBuilder()
				.buildStatus("fake status")
				.build();
		
		assertEquals(MeasureReportStatus.COMPLETE, report.getStatus());
	}
}
