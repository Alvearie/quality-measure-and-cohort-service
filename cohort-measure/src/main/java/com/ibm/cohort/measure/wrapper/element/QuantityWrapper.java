/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

import com.ibm.cohort.measure.wrapper.element.ElementWrapper;

import java.math.BigDecimal;

public interface QuantityWrapper extends ElementWrapper {

    void setValue(BigDecimal value);

    void setCode(String code);

    void setSystem(String system);

}
