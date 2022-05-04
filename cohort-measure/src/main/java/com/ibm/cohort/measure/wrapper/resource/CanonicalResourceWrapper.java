/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.resource;

import com.ibm.cohort.measure.wrapper.element.IdentifierWrapper;

import java.util.List;

public interface CanonicalResourceWrapper extends DomainResourceWrapper {

    String getName();
    void setName(String name);

    String getUrl();
    String setUrl(String url);

    String getVersion();

    List<IdentifierWrapper> getIdentifiers();
    void addIdentifier(IdentifierWrapper identifierWrapper);

}
