/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import java.util.Collections;
import java.util.Map;

public class CqlEvaluationResult {
    private Map<String,Object> resultByExpression;
    
    public CqlEvaluationResult(Map<String,Object> results) {
        this.resultByExpression = results;
    }
    
    public Map<String,Object> getExpressionResults() {
        return Collections.unmodifiableMap(resultByExpression);
    }
}
