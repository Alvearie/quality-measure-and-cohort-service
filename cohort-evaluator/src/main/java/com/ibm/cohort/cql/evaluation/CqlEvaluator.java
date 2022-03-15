/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.elm.execution.ExpressionDef;
import org.cqframework.cql.elm.execution.FunctionDef;
import org.opencds.cqf.cql.engine.data.ExternalFunctionProvider;
import org.opencds.cqf.cql.engine.execution.Context;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.library.CqlLibraryDeserializationException;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlVersionedIdentifier;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;

public class CqlEvaluator {
    
    private static final CqlDebug DEFAULT_CQL_DEBUG = CqlDebug.NONE;
    
    private CqlLibraryProvider libraryProvider;
    private CqlDataProvider dataProvider;
    private CqlTerminologyProvider terminologyProvider;
    private ExternalFunctionProvider externalFunctionProvider;
    private boolean cacheContexts = true;
    
    public List<Pair<CqlEvaluationRequest,CqlEvaluationResult>> evaluate( CqlEvaluationRequests requests ) {
        return evaluate(requests, DEFAULT_CQL_DEBUG);
    }
    
    public List<Pair<CqlEvaluationRequest,CqlEvaluationResult>> evaluate( CqlEvaluationRequests requests, CqlDebug debug ) {
        List<Pair<CqlEvaluationRequest,CqlEvaluationResult>> results = new ArrayList<>(requests.getEvaluations().size());
        ZonedDateTime batchDateTime = ZonedDateTime.now();
        for( CqlEvaluationRequest request : requests.getEvaluations() ) {
            
            
            Map<String,Parameter> parameters = new HashMap<>();
            if( requests.getGlobalParameters() != null ) {
                parameters.putAll(requests.getGlobalParameters());
            }
            if( request.getParameters() != null ) {
                parameters.putAll(request.getParameters());
            }
            
            CqlEvaluationRequest withGlobals = new CqlEvaluationRequest(request);
            withGlobals.setParameters(parameters);
            
            results.add( Pair.of(request, evaluate( withGlobals, debug, batchDateTime )) );
        }
        return results;
    }
    
    public CqlEvaluationResult evaluate( CqlEvaluationRequest request ) {
        return evaluate( request.getDescriptor().getVersionedIdentifier(), request.getParameters(), Pair.of(request.getContextKey(), request.getContextValue()), request.getExpressionNames(), DEFAULT_CQL_DEBUG, null );
    }
    
    public CqlEvaluationResult evaluate( CqlEvaluationRequest request, CqlDebug debug ) {
        return evaluate( request.getDescriptor().getVersionedIdentifier(), request.getParameters(), Pair.of(request.getContextKey(), request.getContextValue()), request.getExpressionNames(), debug, null );
    }

    public CqlEvaluationResult evaluate( CqlEvaluationRequest request, CqlDebug debug, ZonedDateTime batchDateTime ) {
        return evaluate( request.getDescriptor().getVersionedIdentifier(), request.getParameters(), Pair.of(request.getContextKey(), request.getContextValue()), request.getExpressionNames(), debug, batchDateTime );
    }

    public CqlEvaluationResult evaluate( CqlVersionedIdentifier topLevelLibraryIdentifier) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibraryIdentifier, null);
    }
    
    public CqlEvaluationResult evaluate( CqlVersionedIdentifier topLevelLibraryIdentifier, Pair<String,String> context) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibraryIdentifier, null, context, null, DEFAULT_CQL_DEBUG, null);
    }
    
    public CqlEvaluationResult evaluate( CqlVersionedIdentifier topLevelLibraryIdentifier, Pair<String,String> context, Set<String> expressions) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibraryIdentifier, null, context, expressions, DEFAULT_CQL_DEBUG, null);
    }
    
    public CqlEvaluationResult evaluate( CqlVersionedIdentifier topLevelLibraryIdentifier, Map<String,Parameter> parameters, Pair<String,String> context) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibraryIdentifier, parameters, context, null, DEFAULT_CQL_DEBUG, null);
    }

    public CqlEvaluationResult evaluate( CqlVersionedIdentifier topLevelLibraryIdentifier, Map<String,Parameter> parameters, Pair<String,String> context, Set<String> expressions) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibraryIdentifier, parameters, context, expressions, DEFAULT_CQL_DEBUG, null);
    }

    public CqlEvaluationResult evaluate(CqlVersionedIdentifier topLevelLibraryIdentifier, Map<String, Parameter> parameters,
            Pair<String, String> context, Set<String> expressions, CqlDebug debug, ZonedDateTime batchDateTime)
            throws CqlLibraryDeserializationException {
        if (this.libraryProvider == null) {
            throw new IllegalArgumentException("Missing libraryProvider");
        }
        else if (this.dataProvider == null) {
            throw new IllegalArgumentException("Missing dataProvider");
        }
        else if (this.terminologyProvider == null) {
            throw new IllegalArgumentException("Missing terminologyProvider");
        }
        else if (topLevelLibraryIdentifier == null) {
            throw new IllegalArgumentException("Missing library identifier");
        }

        CqlContextFactory contextFactory = new CqlContextFactory();
        contextFactory.setExternalFunctionProvider(this.externalFunctionProvider);
        contextFactory.setCacheContexts(cacheContexts);

        Context cqlContext = contextFactory.createContext(libraryProvider, topLevelLibraryIdentifier,
                terminologyProvider, dataProvider, batchDateTime, context, parameters, debug);
        
        if( expressions == null ) {
            expressions = cqlContext
                    .getCurrentLibrary()
                    .getStatements()
                    .getDef()
                    .stream()
                    .map(ExpressionDef::getName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        Map<String,Object> results = new LinkedHashMap<>();
        for( String expression : expressions ) {
            ExpressionDef expressionDef = cqlContext.resolveExpressionRef(expression);
            // FunctionDefs cannot be evaluated directly.
            // The CqlEngine class from the DBCG codebase, which we used in the past,
            // also explicitly skips over FunctionDefs.
            if (!(expressionDef instanceof FunctionDef)) {
                Object result = cqlContext.resolveExpressionRef(expression).evaluate(cqlContext);
                results.put(expression, result);
            }
        }
        
        if( context != null ) {
            cqlContext.setContextValue(context.getLeft(), context.getRight());
        }
        
        return new CqlEvaluationResult(results);
    }

    public CqlLibraryProvider getLibraryProvider() {
        return libraryProvider;
    }

    public CqlEvaluator setLibraryProvider(CqlLibraryProvider libraryProvider) {
        this.libraryProvider = libraryProvider;
        return this;
    }

    public CqlDataProvider getDataProvider() {
        return dataProvider;
    }

    public CqlEvaluator setDataProvider(CqlDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        return this;
    }
    
    public CqlTerminologyProvider getTerminologyProvider() {
        return terminologyProvider;
    }

    public CqlEvaluator setTerminologyProvider(CqlTerminologyProvider terminologyProvider) {
        this.terminologyProvider = terminologyProvider;
        return this;
    }

    public CqlEvaluator setExternalFunctionProvider(ExternalFunctionProvider externalFunctionProvider) {
        this.externalFunctionProvider = externalFunctionProvider;
        return this;
    }

    public boolean isCacheContexts() {
        return cacheContexts;
    }

    public CqlEvaluator setCacheContexts(boolean cacheContexts) {
        this.cacheContexts = cacheContexts;
        return this;
    }

}
