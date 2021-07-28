/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.model;

import java.util.Set;

/**
 * DataRow is a simple interface for accessing data in tabular form. Fields
 * are assumed to be indexed based on field name strings and values in the
 * row are accessible through the getValue method provided a specific field 
 * name.
 */
public interface DataRow {

    Object getValue(String fieldName);

    Set<String> getFieldNames();
}
