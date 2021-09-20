/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import java.util.List;

public class ContextDefinitions {
    private List<ContextDefinition> contextDefinitions;

    public List<ContextDefinition> getContextDefinitions() {
        return contextDefinitions;
    }

    public void setContextDefinitions(List<ContextDefinition> contextDefinitions) {
        this.contextDefinitions = contextDefinitions;
    }
}
