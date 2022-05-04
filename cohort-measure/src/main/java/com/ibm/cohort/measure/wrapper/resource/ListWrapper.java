/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.resource;

import com.ibm.cohort.measure.wrapper.element.ListEntryWrapper;

import java.util.List;

public interface ListWrapper extends DomainResourceWrapper {

    List<ListEntryWrapper> getEntries();
    void addEntry(ListEntryWrapper entry);

    String getTitle();
    void setTitle(String title);

}
