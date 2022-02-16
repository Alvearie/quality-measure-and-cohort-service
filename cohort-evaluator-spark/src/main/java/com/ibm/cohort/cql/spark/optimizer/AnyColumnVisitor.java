/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import javax.xml.namespace.QName;

import org.hl7.elm.r1.As;
import org.hl7.elm.r1.FunctionRef;
import org.hl7.elm.r1.Literal;

import com.ibm.cohort.cql.functions.AnyColumnFunctions;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.util.PrefixStringMatcher;
import com.ibm.cohort.cql.util.RegexStringMatcher;
import com.ibm.cohort.cql.util.StringMatcher;

/**
 * This is an ELM tree visitor that attempts to locate use of our custom "AnyColumn" functions
 * and return <code>ColumnNameMatcher</code> objects that will property match the columns 
 * needed to support those function calls.
 */
public class AnyColumnVisitor extends PathCaptureVisitor<AnyColumnContext> {

    public AnyColumnVisitor(CqlLibraryProvider provider) {
        super(provider);
    }
    
    @Override
    public Object visitFunctionRef(FunctionRef elm, AnyColumnContext context) {
        if( AnyColumnFunctions.FUNCTION_NAMES.contains( elm.getName() ) ) {
            if( elm.getOperand().size() == 2 ) {
                QName dataType = ((As)elm.getOperand().get(0)).getOperand().getResultTypeName();
                // TODO - validate that the first operand is a model object. We really should be doing that at the
                // method declaration level instead of Choice<Any>, but that will require the model
                // to have a base class that everything extends from.
                
                String columnMatchLogic = null;
                if( elm.getOperand().get(1) instanceof Literal ) {
                    columnMatchLogic = ((Literal) elm.getOperand().get(1)).getValue();
                } else { 
                    throw new IllegalArgumentException(String.format("Second argument to %s function at %s must be a literal", elm.getName(), elm.getLocator()));
                }
                
                StringMatcher matcher = null;
                if( elm.getName().equals(AnyColumnFunctions.FUNC_ANY_COLUMN) ) {
                    matcher = new PrefixStringMatcher(columnMatchLogic);
                } else if( elm.getName().equals(AnyColumnFunctions.FUNC_ANY_COLUMN_REGEX) ) { 
                    matcher = new RegexStringMatcher(columnMatchLogic);
                } else {
                    throw new IllegalArgumentException(String.format("Found declared, but unsupported AnyColumn function %s at %s", elm.getName(), elm.getLocator()));
                }
                
                context.reportAnyColumn(dataType, matcher);
            } else {
                // todo: [daniel.kim]
//                throw new IllegalArgumentException(String.format("%s function at %s should have exactly two arguments", elm.getName(), elm.getLocator()));
            }
        }
        return super.visitFunctionRef(elm, context);
    }

}
