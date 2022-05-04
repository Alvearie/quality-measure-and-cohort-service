/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.resource;

import com.ibm.cohort.measure.MeasureReportType;
import com.ibm.cohort.measure.wrapper.element.MeasureReportGroupWrapper;
import com.ibm.cohort.measure.wrapper.element.ReferenceWrapper;
import org.opencds.cqf.cql.engine.runtime.Interval;

import java.util.List;

public interface MeasureReportWrapper extends DomainResourceWrapper {

    // KWAS TODO: Should this be an enum on our side?
    void setStatus(String status);

    void setType(MeasureReportType type);

    void setMeasure(String uri);

    void setSubject(ReferenceWrapper reference);

    void setPeriod(Interval period);

    void addGroup(MeasureReportGroupWrapper measureGroup);

    void setEvaluatedResource(List<ReferenceWrapper> references);

}
