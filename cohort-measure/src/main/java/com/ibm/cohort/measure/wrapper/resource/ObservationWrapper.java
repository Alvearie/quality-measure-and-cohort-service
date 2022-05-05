/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.resource;

import com.ibm.cohort.measure.wrapper.enums.ObservationStatus;
import com.ibm.cohort.measure.wrapper.BaseWrapper;
import com.ibm.cohort.measure.wrapper.element.CodeableConceptWrapper;

public interface ObservationWrapper extends DomainResourceWrapper {

    void setStatus(ObservationStatus status);

    void setValue(BaseWrapper element);

    void setCode(CodeableConceptWrapper codeableConcept);

}
