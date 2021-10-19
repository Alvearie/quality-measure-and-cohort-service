/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.util;

public class EqualsStringMatcher implements StringMatcher {
    private String columnName;

    public EqualsStringMatcher(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return this.columnName;
    }

    @Override
    public boolean test(String fieldName) {
        return fieldName.equals(columnName);
    }

    @Override
    public boolean equals(Object o2) {
        return o2 instanceof EqualsStringMatcher
                && this.columnName.equals(((EqualsStringMatcher) o2).getColumnName());
    }

    @Override
    public int hashCode() {
        return this.columnName.hashCode();
    }

    @Override
    public String toString() {
        return this.columnName;
    }
}
