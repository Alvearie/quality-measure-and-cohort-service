package com.ibm.cohort.engine.translation;

import org.cqframework.cql.elm.execution.And;
import org.opencds.cqf.cql.engine.elm.execution.AndEvaluator;
import org.opencds.cqf.cql.engine.execution.Context;

public class ShortAndEvaluator extends And {

    @Override
    protected Object internalEvaluate(Context context) {
        Boolean left = getValue(0, context);

        Boolean right;
        if (left == null) {
            // Proliferate null if left is null.
            // We will need to figure out when defaulting right to null is appropriate.
            right = null;
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

    private Boolean getValue(int idx, Context context) {
        return (Boolean)getOperand().get(idx).evaluate(context);
    }

}
