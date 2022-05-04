/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface ListEntryWrapper extends ElementWrapper {

    ReferenceWrapper getItem();
    void setItem(ReferenceWrapper reference);

}
