/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

import com.ibm.cohort.measure.wrapper.enums.ParameterUse;

public interface ParameterDefinitionWrapper extends ElementWrapper {

    void setName(String name);
    String getName();

    void setUse(ParameterUse use);

    void setType(String type);

}
