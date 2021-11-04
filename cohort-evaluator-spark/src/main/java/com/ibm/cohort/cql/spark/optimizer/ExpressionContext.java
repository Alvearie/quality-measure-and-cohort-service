/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.util.ArrayDeque;
import java.util.Deque;

import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Query;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * Maintains state for an expression definition so that aliases and let
 * references can be resolved by other parts of the expression
 */
public class ExpressionContext {

    private VersionedIdentifier libraryIdentifier;
    private ExpressionDef expression;

    public ExpressionContext(VersionedIdentifier libraryIdentifier, ExpressionDef elm) {
        this.libraryIdentifier = libraryIdentifier;
        this.expression = elm;
    }
    
    public VersionedIdentifier getLibraryIdentifier() {
        return libraryIdentifier;
    }
    public ExpressionDef getExpression() {
        return expression;
    }
    
    private Deque<QueryContext> queryContextStack = new ArrayDeque<>();
    public void enterQueryContext(Query query) {
        QueryContext queryContext = new QueryContext(query);
        queryContextStack.push(queryContext);
    }
    public QueryContext getCurrentQueryContext() {
        return queryContextStack.peek();
    }
    public QueryContext exitQueryContext() {
        return queryContextStack.pop();
    }
    public QueryLetContext resolveLet(String letName) {
        QueryLetContext letContext = null;
        for( QueryContext queryContext : queryContextStack ) {
            letContext = queryContext.resolveLet(letName);
            if( letContext != null ) {
                break;
            }
        }
        
        if( letContext == null ) {
            throw new IllegalArgumentException(String.format("Could not resolve let %s", letName));
        }
        
        return letContext;
    }
    
    public QueryAliasContext resolveAlias(String aliasName) {
        QueryAliasContext aliasContext = null;
        for( QueryContext queryContext : queryContextStack ) {
            aliasContext = queryContext.resolveAlias(aliasName);
            if( aliasContext != null ) {
                break;
            }
        }
        
        if( aliasContext == null ) {
            throw new IllegalArgumentException(String.format("Could not resolve alias %s", aliasName));
        }
        
        return aliasContext;
    }
}
