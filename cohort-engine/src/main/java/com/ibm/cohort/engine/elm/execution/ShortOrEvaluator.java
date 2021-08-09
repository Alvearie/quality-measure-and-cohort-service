/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.ibm.cohort.engine.elm.execution;

import org.cqframework.cql.elm.execution.Or;
import org.opencds.cqf.cql.engine.elm.execution.OrEvaluator;
import org.opencds.cqf.cql.engine.execution.Context;

public class ShortOrEvaluator extends Or {

    @Override
    protected Object internalEvaluate(Context context) {
        Boolean left = getValue(0, context);

        Boolean right;
        if (left == null) {
	        // Evaluate to be consistent with base logic
            right = getValue(1, context);
        }
        else if (left) {
            // Default to false because left is already true
            // thus we don't care about the result of right.
            right = Boolean.FALSE;
        }
        else {
            right = getValue(1, context);
        }

        return OrEvaluator.or(left, right);
    }

    protected Boolean getValue(int idx, Context context) {
        return (Boolean)getOperand().get(idx).evaluate(context);
    }

}
