/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import java.util.List;
import java.util.stream.Collectors;

public class ContextDefinitions {
    private List<ContextDefinition> contextDefinitions;

    public List<ContextDefinition> getContextDefinitions() {
        return contextDefinitions;
    }

    public void setContextDefinitions(List<ContextDefinition> contextDefinitions) {
        this.contextDefinitions = contextDefinitions;
    }

    /**
     * 
     * @param contextName   Context name used to retrieve a context definition
     * @return  Returns the ContextDefinition for the corresponding context name
     *          if exactly one definition is found. Returns null if no definition
     *          is found for the given context name.
     *          Throws an IllegalArgumentException if multiple context definitions
     *          exist for the provided context name.
     */
    public ContextDefinition getContextDefinitionByName(String contextName) {
        List<ContextDefinition> definitions = contextDefinitions.stream()
                .filter(x -> x.getName() != null && x.getName().equals(contextName))
                .collect(Collectors.toList());

        if (definitions.size() > 1) {
            throw new IllegalArgumentException("A context must be defined exactly once in the context definitions file. Found "
                                                       + definitions.size() + " definitions for context: " + contextName);
        }
        else if (definitions.isEmpty()) {
            return null;
        }
        else {
            return definitions.get(0);
        }
    }
}
