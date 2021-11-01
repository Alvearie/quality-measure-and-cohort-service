/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.hl7.elm.r1.AliasRef;
import org.hl7.elm.r1.ByColumn;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Property;
import org.hl7.elm.r1.QueryLetRef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.UsingDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains state for an ELM traversal that records the model objects and any
 * property dereferences that are made against those model objects.
 */
public class PathCaptureContext {
    private static final Logger LOG = LoggerFactory.getLogger(PathCaptureContext.class);
    
    private Stack<ExpressionContext> expressionContextStack = new Stack<ExpressionContext>();
    public void enterExpressionContext(VersionedIdentifier libraryIdentifier, ExpressionDef elm) {
        ExpressionContext queryContext = new ExpressionContext(libraryIdentifier, elm);
        expressionContextStack.push(queryContext);
    }
    
    public ExpressionContext getCurrentExpressionContext() {
        return expressionContextStack.peek();
    }
    public QueryContext getCurrentQueryContext() {
        return getCurrentExpressionContext().getCurrentQueryContext();
    }    
    public ExpressionContext exitExpressionContext() {
        return expressionContextStack.pop();
    }
    
    private Map<QName,Set<String>> pathsByQName = new HashMap<>();
    public Map<QName,Set<String>> getPathsByQName() {
        return pathsByQName;
    }
    
    private Map<String, VersionedIdentifier> modelIdByUri = new HashMap<>();
    public Map<String,VersionedIdentifier> getModels() {
        return modelIdByUri;
    }
    
    public void reportProperty(Property elm) {
        LOG.trace("Property {} source {}", elm.getPath(), elm.getSource() != null ? elm.getSource().getClass().getSimpleName() : null);
        Set<QName> modelTypeNames = null;
        if( elm.getScope() != null || elm.getSource() instanceof AliasRef ) {
            String aliasName = (elm.getScope() != null) ? elm.getScope() : ((AliasRef)elm.getSource()).getName();
            QueryAliasContext aliasContext = getCurrentQueryContext().resolveAlias(aliasName);
            if( aliasContext == null ) {
                aliasContext = getCurrentExpressionContext().resolveAlias(aliasName);
            }
            modelTypeNames = ElmUtils.getModelTypeNames(aliasContext.getAliasedQuerySource().getExpression());
        } else if( elm.getSource() instanceof QueryLetRef ) { 
            String letName = ((QueryLetRef)elm.getSource()).getName();
            QueryLetContext letContext = getCurrentQueryContext().resolveLet(letName);
            if( letContext == null ) {
                letContext = getCurrentExpressionContext().resolveLet(letName);
            }
            modelTypeNames = ElmUtils.getModelTypeNames(letContext.getLetClause().getExpression());
        } else {
            // There are times when the scope is null. I've noticed this particularly when referencing properties
            // of another expression result
            modelTypeNames = ElmUtils.getModelTypeNames(elm.getSource());
        }
        LOG.trace("ModelTypeNames {}", modelTypeNames);
        
        if( modelTypeNames != null ) {
            for( QName qname : modelTypeNames ) { 
                pathsByQName.computeIfAbsent(qname, key -> new HashSet<>()).add(elm.getPath());
            }
        }
    }
    

    public void reportRetrieve(Retrieve retrieve) {
        Set<String> pathsByType = pathsByQName.computeIfAbsent(retrieve.getDataType(), key -> new HashSet<>());
        CollectionUtils.addIgnoreNull(pathsByType, retrieve.getIdProperty());
        CollectionUtils.addIgnoreNull(pathsByType, retrieve.getContextProperty());
        CollectionUtils.addIgnoreNull(pathsByType, retrieve.getCodeProperty());
        CollectionUtils.addIgnoreNull(pathsByType, retrieve.getDateProperty());
        CollectionUtils.addIgnoreNull(pathsByType, retrieve.getDateLowProperty());
        CollectionUtils.addIgnoreNull(pathsByType, retrieve.getDateHighProperty());
    }

    public void reportByColumn(ByColumn elm) {
        Set<QName> modelTypeNames = ElmUtils.getModelTypeNames(getCurrentQueryContext().getQuery());
        for( QName name : modelTypeNames ) {
            pathsByQName.computeIfAbsent(name, key -> new HashSet<>()).add( elm.getPath() );
        }
    }

    public void reportUsingDef(UsingDef elm) {
        if( ! elm.getUri().equals(CqlConstants.SYSTEM_MODEL_URI) ) {
            VersionedIdentifier vid = new VersionedIdentifier().withId(elm.getLocalIdentifier())
                    .withVersion(elm.getVersion());
            
            modelIdByUri.put( elm.getUri(), vid );
        }
    }
}
