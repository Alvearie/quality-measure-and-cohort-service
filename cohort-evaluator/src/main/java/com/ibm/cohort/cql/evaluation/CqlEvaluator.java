/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.opencds.cqf.cql.engine.execution.Context;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDeserializationException;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;

public class CqlEvaluator {
    
    private static final CqlDebug DEFAULT_CQL_DEBUG = CqlDebug.NONE;
    
    private CqlLibraryProvider libraryProvider;
    private CqlDataProvider dataProvider;
    private CqlTerminologyProvider terminologyProvider;
    
    public List<Pair<CqlEvaluationRequest,CqlEvaluationResult>> evaluate( CqlEvaluationRequests requests ) {
        return evaluate(requests, DEFAULT_CQL_DEBUG);
    }
    
    public List<Pair<CqlEvaluationRequest,CqlEvaluationResult>> evaluate( CqlEvaluationRequests requests, CqlDebug debug ) {
        List<Pair<CqlEvaluationRequest,CqlEvaluationResult>> results = new ArrayList<>(requests.getEvaluations().size());
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
            
            results.add( Pair.of(request, evaluate( withGlobals, debug )) );
        }
        return results;
    }
    
    public CqlEvaluationResult evaluate( CqlEvaluationRequest request ) {
        return evaluate( request.getDescriptor(), request.getParameters(), Pair.of(request.getContextKey(), request.getContextValue()), request.getExpressionNames(), DEFAULT_CQL_DEBUG );
    }
    
    public CqlEvaluationResult evaluate( CqlEvaluationRequest request, CqlDebug debug ) {
        return evaluate( request.getDescriptor(), request.getParameters(), Pair.of(request.getContextKey(), request.getContextValue()), request.getExpressionNames(), debug );
    }
    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibrary, null);
    }
    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary, Pair<String,String> context) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibrary, null, context, null, DEFAULT_CQL_DEBUG);
    }
    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary, Pair<String,String> context, Set<String> expressions) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibrary, null, context, expressions, DEFAULT_CQL_DEBUG);
    }
    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary, Map<String,Parameter> parameters, Pair<String,String> context) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibrary, parameters, context, null, DEFAULT_CQL_DEBUG);
    }

    
    public CqlEvaluationResult evaluate(CqlLibraryDescriptor topLevelLibrary, Map<String, Parameter> parameters,
            Pair<String, String> context, Set<String> expressions, CqlDebug debug)
            throws CqlLibraryDeserializationException {
        Context cqlContext = new CqlContextFactory().createContext(libraryProvider, topLevelLibrary,
                terminologyProvider, dataProvider, null, context, parameters, debug);
        
        if( expressions == null ) {
            expressions = cqlContext.getCurrentLibrary().getStatements().getDef().stream().map( d -> d.getName() ).collect(Collectors.toSet());
        }
        
        Map<String,Object> results = new HashMap<>();
        for( String expression : expressions ) {
            Object result = cqlContext.resolveExpressionRef(expression).evaluate(cqlContext);
            results.put(expression, result);
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

}
