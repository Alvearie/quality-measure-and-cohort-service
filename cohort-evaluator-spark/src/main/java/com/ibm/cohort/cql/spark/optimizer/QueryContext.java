/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.util.HashMap;
import java.util.Map;

import org.hl7.elm.r1.AliasedQuerySource;
import org.hl7.elm.r1.LetClause;
import org.hl7.elm.r1.Query;

/**
 * Maintains state for the various parts of a Query expression so that
 * other parts of the query can following aliases.
 */
public class QueryContext {

    private Query query;
    private QueryAliasContext definitionContext;
    private Map<String, QueryAliasContext> aliases = new HashMap<>();
    private QueryLetContext letDefinitionContext;
    private Map<String, QueryLetContext> letContexts = new HashMap<>();
    
    public QueryContext(Query query) {
        this.query = query;
    }
    public Query getQuery() {
        return this.query;
    }
    
    public void enterAliasDefinitionContext(AliasedQuerySource elm) {
        if( definitionContext != null ) {
            throw new IllegalArgumentException("Alias definition already in progress");
        }
        definitionContext = new QueryAliasContext(elm);
    }
    
    public void exitAliasDefinitionContext() {
        if( definitionContext == null ) {
            throw new IllegalArgumentException("No definition in progress");
        }
        
        aliases.put(definitionContext.getAlias(), definitionContext);
        
        definitionContext = null;
    }
    
    public void enterLetDefinitionContext(LetClause elm) {
        if( letDefinitionContext != null ) {
            throw new IllegalArgumentException("Let definition already in progress");
        }
        letDefinitionContext = new QueryLetContext(elm);
    }
    
    public void exitLetDefinitionContext() {
        if( letDefinitionContext == null ) {
            throw new IllegalArgumentException("No definition in progress");
        }
        
        letContexts.put(letDefinitionContext.getIdentifier(), letDefinitionContext);
        
        letDefinitionContext = null;
    }
    
    public QueryAliasContext resolveAlias(String aliasName) {
        if( definitionContext != null && aliasName.equals(definitionContext.getAlias()) ) {
            return definitionContext;
        } else {
            return aliases.get(aliasName);
        }
    }

    public QueryLetContext resolveLet(String letName) {
        return letContexts.get(letName);
    }
}
