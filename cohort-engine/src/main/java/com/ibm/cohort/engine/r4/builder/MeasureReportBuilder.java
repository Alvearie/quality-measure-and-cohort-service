/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 * 
 * Originated from org.opencds.cqf.r4.builders.MeasureReportBuilder  
 */

package com.ibm.cohort.engine.r4.builder;

import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.opencds.cqf.common.builders.BaseBuilder;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.ibm.cohort.annotations.Generated;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;

public class MeasureReportBuilder extends BaseBuilder<MeasureReport> {
    public MeasureReportBuilder() {
        super(new MeasureReport());
    }

    @Generated
    public MeasureReportBuilder buildStatus(String status) {
        try {
            this.complexProperty.setStatus(MeasureReport.MeasureReportStatus.fromCode(status));
        } catch (FHIRException e) {
            // default to complete
            this.complexProperty.setStatus(MeasureReport.MeasureReportStatus.COMPLETE);
        }
        return this;
    }

    @Generated
    public MeasureReportBuilder buildType(MeasureReport.MeasureReportType type) {
        this.complexProperty.setType(type);
        return this;
    }

    @Generated
    public MeasureReportBuilder buildType(String type) {
        this.complexProperty.setType(MeasureReport.MeasureReportType.fromCode(type));
        return this;
    }

    @Generated
    public MeasureReportBuilder buildMeasureReference(String measureRef) {
        this.complexProperty.setMeasure(measureRef);
        return this;
    }

    @Generated
    public MeasureReportBuilder buildPatientReference(String patientRef) {
        this.complexProperty.setSubject(new Reference(patientRef));
        return this;
    }

    @Generated
    public MeasureReportBuilder buildPeriod(Interval period) {
        Object start = period.getStart();
        if (start instanceof DateTime) {
            this.complexProperty
                    .setPeriod(new Period()
                               .setStartElement(new DateTimeType( ((DateTime) period.getStart()).toJavaDate(), TemporalPrecisionEnum.MILLI, TimeZone.getTimeZone(ZoneId.of("Z"))))
                               .setEndElement(new DateTimeType( ((DateTime) period.getEnd()).toJavaDate(), TemporalPrecisionEnum.MILLI, TimeZone.getTimeZone(ZoneId.of("Z")))));
        } else if (start instanceof Date) {
            this.complexProperty
                    .setPeriod(new Period()
                               .setStartElement(new DateTimeType( (Date) period.getStart(), TemporalPrecisionEnum.MILLI, TimeZone.getTimeZone(ZoneId.of("Z"))))
                               .setEndElement(new DateTimeType( (Date) period.getEnd(), TemporalPrecisionEnum.MILLI, TimeZone.getTimeZone(ZoneId.of("Z"))))); 
        }

        return this;
    }
}