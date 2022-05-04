/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.resource;

import com.ibm.cohort.measure.ObservationStatus;
import com.ibm.cohort.measure.wrapper.element.ElementWrapper;

public interface ObservationWrapper extends DomainResourceWrapper {

    void setStatus(ObservationStatus status);

    void setValue(ElementWrapper element);

}
