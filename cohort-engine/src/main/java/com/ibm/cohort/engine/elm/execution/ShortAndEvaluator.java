package com.ibm.cohort.engine.elm.execution;

import org.cqframework.cql.elm.execution.And;
import org.opencds.cqf.cql.engine.elm.execution.AndEvaluator;
import org.opencds.cqf.cql.engine.execution.Context;

public class ShortAndEvaluator extends And {

    @Override
    protected Object internalEvaluate(Context context) {
        Boolean left = getValue(0, context);

        Boolean right;
        if (left == null) {
            // Evaluate to be consistent with base logic
            right = getValue(1, context);
        }
        else if (left) {
            right = getValue(1, context);
        }
        else {
            // Default to false because left is already false
            // thus we don't care about the result of right.
            right = Boolean.FALSE;
        }

        return AndEvaluator.and(left, right);
    }

    protected Boolean getValue(int idx, Context context) {
        return (Boolean)getOperand().get(idx).evaluate(context);
    }

}
