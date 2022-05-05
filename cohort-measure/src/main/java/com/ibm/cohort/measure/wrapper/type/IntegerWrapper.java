/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.type;

import com.ibm.cohort.measure.wrapper.BaseWrapper;

public interface IntegerWrapper extends BaseWrapper {

    // KWAS TODO: nullable???
    void setValue(int value);

}
