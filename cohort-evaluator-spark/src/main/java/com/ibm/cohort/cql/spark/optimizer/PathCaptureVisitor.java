/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import org.hl7.elm.r1.AliasedQuerySource;
import org.hl7.elm.r1.ByColumn;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.LetClause;
import org.hl7.elm.r1.Property;
import org.hl7.elm.r1.Query;
import org.hl7.elm.r1.Retrieve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.cql.library.CqlLibraryProvider;

/**
 * Implements an ELM visitor that walks the ELM tree, following expression
 * references both local and in included libraries, and captures which model
 * objects are used and any property paths that are used to dereference those
 * model objects.
 */
public class PathCaptureVisitor <C extends PathCaptureContext> extends GraphWalkingElmVisitor<Object,C> {

    private static final Logger LOG = LoggerFactory.getLogger(PathCaptureVisitor.class);

    public PathCaptureVisitor(CqlLibraryProvider libraryProvider) {
        super(libraryProvider);
    }

    @Override
    public Object visitProperty(Property elm, C context) {
        context.reportProperty(elm);
        return super.visitProperty(elm, context);
    }

    @Override
    public Object visitRetrieve(Retrieve elm, C context) {
        context.reportRetrieve(elm);
        return super.visitRetrieve(elm, context);
    }

    @Override
    public Object visitQuery(Query elm, C context) {
        context.getCurrentExpressionContext().enterQueryContext(elm);
        try {
             return super.visitQuery(elm, context);
        } finally {
            context.getCurrentExpressionContext().exitQueryContext();
        }
    }
    
    @Override
    public Object visitAliasedQuerySource(AliasedQuerySource elm, C context) {
        context.getCurrentQueryContext().enterAliasDefinitionContext(elm);
        try {
            return super.visitAliasedQuerySource(elm, context);
        } finally {
            context.getCurrentQueryContext().exitAliasDefinitionContext();
        }
    }

    @Override
    public Object visitLetClause(LetClause elm, C context) {
        context.getCurrentQueryContext().enterLetDefinitionContext(elm);
        try {
            return super.visitLetClause(elm, context);
        } finally {
            context.getCurrentQueryContext().exitLetDefinitionContext();
        }
    }

    @Override
    public Object visitExpressionDef(ExpressionDef elm, C context) {
        LOG.trace("--> {}:{}", getCurrentLibraryIdentifier().getId(), elm.getName());
        context.enterExpressionContext(getCurrentLibraryIdentifier(), elm);
        Object result;
        try {
            result = super.visitExpressionDef(elm, context);
        } finally {
            context.exitExpressionContext();
        }
        LOG.trace("<-- {}:{}", getCurrentLibraryIdentifier().getId(), elm.getName());
        return result;
    }
    
    @Override
    public Object visitByColumn(ByColumn elm, C context) {
        context.reportByColumn(elm);
        return super.visitByColumn(elm, context);
    }  
}
