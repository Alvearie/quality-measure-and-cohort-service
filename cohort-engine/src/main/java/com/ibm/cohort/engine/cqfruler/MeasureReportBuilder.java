/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 * 
 * Originated from org.opencds.cqf.r4.builders.MeasureReportBuilder  
 */

package com.ibm.cohort.engine.cqfruler;

import java.util.Date;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.opencds.cqf.common.builders.BaseBuilder;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.ibm.cohort.annotations.Generated;

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
                    .setPeriod(new Period().setStart(Date.from(((DateTime) start).getDateTime().toInstant()))
                            .setEnd(Date.from(((DateTime) period.getEnd()).getDateTime().toInstant())));
        } else if (start instanceof Date) {
            this.complexProperty
                    .setPeriod(new Period().setStart((Date) period.getStart()).setEnd((Date) period.getEnd()));
        }

        return this;
    }
}