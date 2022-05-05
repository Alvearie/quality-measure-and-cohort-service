/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.type;

import com.ibm.cohort.measure.wrapper.BaseWrapper;

import java.time.LocalTime;

public interface TimeWrapper extends BaseWrapper {

    void setValue(LocalTime value);

}
