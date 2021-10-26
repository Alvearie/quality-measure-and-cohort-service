/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PrefixStringMatcher implements StringMatcher {
    private String prefix;

    public PrefixStringMatcher(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public boolean test(String fieldName) {
        return fieldName.startsWith(prefix);
    }

    @Override
    public boolean equals(Object o2) {
        return o2 instanceof PrefixStringMatcher && this.prefix.equals(((PrefixStringMatcher) o2).getPrefix());
    }

    @Override
    public int hashCode() {
        return this.prefix.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("prefix", this.prefix)
                .toString();
    }
}
