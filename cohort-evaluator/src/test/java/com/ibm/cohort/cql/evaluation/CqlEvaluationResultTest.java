/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class CqlEvaluationResultTest {
    @Test
    public void testSetterGetter() {
        Map<String,Object> resultByExpression = new HashMap<>();
        resultByExpression.put("Age", 10);
        resultByExpression.put("Gender", "female");
        resultByExpression.put("Conditions", Arrays.asList("diabetes", "glaucoma", "neuropathy"));
        
        CqlEvaluationResult result = new CqlEvaluationResult(resultByExpression);
        assertEquals(resultByExpression, result.getExpressionResults());    
    }
}
