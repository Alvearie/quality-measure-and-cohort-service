/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

// KWAS TODO: Walk through wrappers to ensure coding shows up where it needs to
// Try to avoid methods that skip past the coding type.
public interface CodingWrapper extends ElementWrapper {

    String getCode();
    void setCode(String code);

    String getSystem();
    void setSystem(String system);

    String getDisplay();
    void setDisplay(String display);

    void setVersion(String version);

}
