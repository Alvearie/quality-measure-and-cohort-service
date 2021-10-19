/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.functions;

import java.util.stream.Collectors;

import com.ibm.cohort.cql.util.StringMatcher;
import com.ibm.cohort.cql.util.PrefixStringMatcher;
import com.ibm.cohort.cql.util.RegexStringMatcher;
import com.ibm.cohort.datarow.model.DataRow;

public class AnyColumnFunctions {

    private AnyColumnFunctions() {
    }

    public static Object AnyColumn(Object object, String fieldPrefix) {
        DataRow dataRow = (DataRow) object;
        
        StringMatcher matcher = new PrefixStringMatcher(fieldPrefix);

        return dataRow.getFieldNames().stream().filter(matcher)
            .map(dataRow::getValue)
            .collect(Collectors.toList());
    }

    public static Object AnyColumnRegex(Object object, String regex) {
        DataRow dataRow = (DataRow) object;
        
        StringMatcher matcher = new RegexStringMatcher(regex);

        return dataRow.getFieldNames().stream().filter(matcher)
            .map(dataRow::getValue)
            .collect(Collectors.toList());
    }
}
