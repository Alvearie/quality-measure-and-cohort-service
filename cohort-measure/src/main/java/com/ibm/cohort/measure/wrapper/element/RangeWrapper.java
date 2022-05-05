/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface RangeWrapper extends ElementWrapper {

    void setLow(QuantityWrapper quantity);

    void setHigh(QuantityWrapper quantity);

}
