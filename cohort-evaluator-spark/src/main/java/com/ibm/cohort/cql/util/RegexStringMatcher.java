/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RegexStringMatcher implements StringMatcher {
    private String regex;

    public RegexStringMatcher(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return this.regex;
    }

    @Override
    public boolean test(String fieldName) {
        return fieldName.matches(regex);
    }

    @Override
    public boolean equals(Object o2) {
        return o2 instanceof RegexStringMatcher && this.regex.equals(((RegexStringMatcher) o2).getRegex());
    }

    @Override
    public int hashCode() {
        return this.regex.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("regex", this.regex)
                .toString();
    }
}
