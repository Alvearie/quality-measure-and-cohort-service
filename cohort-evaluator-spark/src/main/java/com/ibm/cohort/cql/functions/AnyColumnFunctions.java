/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.functions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Tuple;

import com.ibm.cohort.cql.util.PrefixStringMatcher;
import com.ibm.cohort.cql.util.RegexStringMatcher;
import com.ibm.cohort.cql.util.StringMatcher;
import com.ibm.cohort.datarow.model.DataRow;

public class AnyColumnFunctions {

    // If you add an additional function here, you should also
    // add support for it under spark/optimizer/AnyColumnVisitor
    public static final String FUNC_ANY_COLUMN_REGEX = "AnyColumnRegex";
    public static final String FUNC_ANY_COLUMN = "AnyColumn";
    
    public static final Set<String> FUNCTION_NAMES;
    static {
        FUNCTION_NAMES = Collections.unmodifiableSet(
                Stream.of(AnyColumnFunctions.class.getDeclaredMethods())
                    .map( method -> method.getName() )
                    .collect(Collectors.toSet()));
    }
    
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


    private static Map<String, String> omopConceptDb = new HashMap<>();
    static {
        omopConceptDb.put("2617212", "G0108");
        omopConceptDb.put("2617211", "G0109");
    }


    private static Map<String, String> omopVocabularyDb = new HashMap<>();
    static {
// todo: [daniel.kim]
    }

    // todo: [daniel.kim]
    public static Object ToCode(Object object) {
        Tuple tuple = (Tuple) object;

        String conceptId = (String) tuple.getElement("concept_id");
        String codeString = omopConceptDb.get(conceptId);

        Code code = new Code().withCode(codeString)
            .withSystem("HCPCS")
            .withVersion("2020");

        return Collections.singletonList(code);
    }
}
