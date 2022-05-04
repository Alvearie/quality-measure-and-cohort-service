/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.resource;

import com.ibm.cohort.measure.wrapper.BaseWrapper;

public interface ResourceWrapper extends BaseWrapper {

    // KWAS TODO: Just the ID or the fully qualified ID???
    String getId();
    void setId(String id);

    String getResourceType();
    // KWAS TODO: I don't think we need a setResourceType...right?

}
