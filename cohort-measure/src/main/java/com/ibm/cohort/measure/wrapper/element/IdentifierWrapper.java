/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface IdentifierWrapper extends ElementWrapper {

    String getValue();
    void setValue(String value);

    String getSystem();
    void setSystem(String system);

}
