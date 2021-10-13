/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import org.cqframework.cql.elm.requirements.ElmRequirement;
import org.cqframework.cql.elm.requirements.ElmRequirementsContext;
import org.cqframework.cql.elm.requirements.ElmRequirementsVisitor;
import org.hl7.elm.r1.If;

public class CustomElmRequirementsVisitor extends ElmRequirementsVisitor {
    // The original code fails to visit all the parts of the IF. There is a 
    // TODO there that suggests more work is required in the future. For now
    // we are reverting back to the default implementation and it seems to work.
    @Override
    public ElmRequirement visitIf(If elm, ElmRequirementsContext context) {
        ElmRequirement result = defaultResult();
        if (elm.getCondition() != null) {
            ElmRequirement childResult = visitElement(elm.getCondition(), context);
            result = aggregateResult(result, childResult);
        }
        if (elm.getThen() != null) {
            ElmRequirement childResult = visitElement(elm.getThen(), context);
            result = aggregateResult(result, childResult);
        }
        if (elm.getElse() != null) {
            ElmRequirement childResult = visitElement(elm.getElse(), context);
            result = aggregateResult(result, childResult);
        }
        return result;
    }
}