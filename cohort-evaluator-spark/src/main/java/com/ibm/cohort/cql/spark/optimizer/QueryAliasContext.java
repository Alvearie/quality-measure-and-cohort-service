/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import org.hl7.elm.r1.AliasedQuerySource;

/**
 * Maintains the state for an aliased query source. This 
 * can be a simple expression alias such as <code>[Encounter] e</code>
 * or a more complex expression such as the <code>with</code> or <code>without</code>
 * clauses of a query.
 */
public class QueryAliasContext {

    private AliasedQuerySource aliasedQuerySource;

    public QueryAliasContext(AliasedQuerySource elm) {
        this.aliasedQuerySource = elm;
    }

    public String getAlias() {
        return aliasedQuerySource.getAlias();
    }

    public AliasedQuerySource getAliasedQuerySource() {
        return this.aliasedQuerySource;
    }

}
