/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.type;

import com.ibm.cohort.measure.wrapper.BaseWrapper;

import java.time.ZonedDateTime;

public interface DateTimeWrapper extends BaseWrapper {

    void setValue(String value);

}
