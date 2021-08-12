/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.opencds.cqf.cql.engine.execution.Context;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDeserializationException;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;

public class CqlEvaluator {
    
    private CqlLibraryProvider libraryProvider;
    private CqlDataProvider dataProvider;
    private CqlTerminologyProvider terminologyProvider;
    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibrary, null);
    }
    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary, Pair<String,String> context) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibrary, null, context, null, CqlDebug.NONE);
    }
    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary, Pair<String,String> context, Set<String> expressions) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibrary, null, context, expressions, CqlDebug.NONE);
    }
    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary, Map<String,Object> parameters, Pair<String,String> context) throws CqlLibraryDeserializationException {
        return evaluate(topLevelLibrary, parameters, context, null, CqlDebug.NONE);
    }

    
    public CqlEvaluationResult evaluate( CqlLibraryDescriptor topLevelLibrary, Map<String,Object> parameters, Pair<String,String> context, Set<String> expressions, CqlDebug debug) throws CqlLibraryDeserializationException {
        Context cqlContext = new CqlContextFactory().setDebug(debug).createContext(libraryProvider, topLevelLibrary, terminologyProvider, dataProvider);
        
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
