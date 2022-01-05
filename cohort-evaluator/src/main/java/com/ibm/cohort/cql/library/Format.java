/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The possible library formats alongside their aliases.
 */
public enum Format {

    CQL("cql", "text/cql", "application/cql"),
    ELM("XML", "xml", "application/elm+xml");

    private final List<String> names;
    private Format(String... aliases) {
        this.names = new ArrayList<>();
        this.names.add(this.name());
        this.names.addAll(Arrays.asList(aliases));
    }

    public List<String> getNames() {
        return this.names;
    }

    private static final Map<String, Format> INDEX = new HashMap<>(Format.values().length);
    static {
        for( Format fmt : Format.values() ) {
            for( String name : fmt.getNames() ) {
                INDEX.put(name, fmt);
            }
        }
    }

    /**
     * Allow client code to do name to enum conversion using the
     * enum's formal name or any of its aliases.
     *
     * @param name name of the enum to lookup
     * @return value of the resolved enum or null if no enum is found matching the name
     */
    public static Format lookupByName(String name) {
        return INDEX.get(name);
    }

}
