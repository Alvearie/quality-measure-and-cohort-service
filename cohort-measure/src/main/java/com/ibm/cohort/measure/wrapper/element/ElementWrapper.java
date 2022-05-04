/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

import com.ibm.cohort.measure.wrapper.BaseWrapper;

import java.util.List;

public interface ElementWrapper extends BaseWrapper {

    // KWAS TODO: Reconcile with ResourceWrapper after everything is said and done.
    String getId();
    void setId(String id);

    List<ExtensionWrapper> getExtension();

}
