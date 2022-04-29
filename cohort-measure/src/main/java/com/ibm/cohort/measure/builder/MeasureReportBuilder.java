/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.builder;

import java.util.Date;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.opencds.cqf.common.builders.BaseBuilder;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.ibm.cohort.annotations.Generated;
import com.ibm.cohort.measure.CQLToFHIRMeasureReportHelper;

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
                               .setStartElement((DateTimeType) CQLToFHIRMeasureReportHelper.getFhirTypeValue(period.getStart()))
                               .setEndElement((DateTimeType) CQLToFHIRMeasureReportHelper.getFhirTypeValue(period.getEnd())));
        } else if (start instanceof Date) {
            DateTime cqlStart = org.opencds.cqf.cql.engine.runtime.Date.fromJavaDate((Date) period.getStart());
            DateTime cqlend = org.opencds.cqf.cql.engine.runtime.Date.fromJavaDate((Date) period.getEnd());

            this.complexProperty
                    .setPeriod(new Period()
                               .setStartElement((DateTimeType) CQLToFHIRMeasureReportHelper.getFhirTypeValue(cqlStart))
                               .setEndElement((DateTimeType) CQLToFHIRMeasureReportHelper.getFhirTypeValue(cqlend))); 
        }

        return this;
    }
}