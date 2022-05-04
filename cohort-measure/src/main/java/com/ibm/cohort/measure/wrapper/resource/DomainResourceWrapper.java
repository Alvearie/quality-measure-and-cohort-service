/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.resource;

import com.ibm.cohort.measure.wrapper.element.ExtensionWrapper;

import java.util.List;

public interface DomainResourceWrapper extends ResourceWrapper {

    List<ResourceWrapper> getContained();
    void addContained(ResourceWrapper resource);

    List<ExtensionWrapper> getExtension();

}
