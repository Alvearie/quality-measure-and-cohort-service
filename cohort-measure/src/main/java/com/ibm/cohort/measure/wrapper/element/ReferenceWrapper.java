/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface ReferenceWrapper extends ElementWrapper {

    String getReference();
    void setReference(String reference);

    String getDisplay();
    void setDisplay(String display);

}
