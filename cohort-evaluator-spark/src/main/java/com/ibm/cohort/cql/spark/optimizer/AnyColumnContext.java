/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.ibm.cohort.cql.util.StringMatcher;

/**
 * This context object is used by the AnyColumnVisitor to collect 
 * data about AnyColumn function usage during an ELM tree traversal. 
 */
public class AnyColumnContext extends PathCaptureContext {

    private Map<QName, Set<StringMatcher>> matchersByDataType = new HashMap<>();

    public Map<QName, Set<StringMatcher>> getMatchers() {
        return Collections.unmodifiableMap(matchersByDataType);
    }
    
    public void reportAnyColumn(QName dataType, StringMatcher matcher) {
        Set<StringMatcher> patterns = matchersByDataType.computeIfAbsent(dataType, key -> new HashSet<>());
        patterns.add( matcher );
    }
}
