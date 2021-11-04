/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import org.hl7.elm.r1.LetClause;

/**
 * Maintains the state for let expressions within a query.
 */
public class QueryLetContext {

    private LetClause letClause;

    public QueryLetContext(LetClause elm) {
        this.letClause = elm;
    }

    public String getIdentifier() {
        return this.letClause.getIdentifier();
    }

    public LetClause getLetClause() {
        return letClause;
    }

}
