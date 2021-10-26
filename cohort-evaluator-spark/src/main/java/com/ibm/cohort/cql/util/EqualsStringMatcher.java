/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class EqualsStringMatcher implements StringMatcher {
    private String expected;

    public EqualsStringMatcher(String expected) {
        this.expected = expected;
    }

    public String getColumnName() {
        return this.expected;
    }

    @Override
    public boolean test(String fieldName) {
        return fieldName.equals(expected);
    }

    @Override
    public boolean equals(Object o2) {
        return o2 instanceof EqualsStringMatcher
                && this.expected.equals(((EqualsStringMatcher) o2).getColumnName());
    }

    @Override
    public int hashCode() {
        return this.expected.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("expected", this.expected)
                .toString();
    }
}
