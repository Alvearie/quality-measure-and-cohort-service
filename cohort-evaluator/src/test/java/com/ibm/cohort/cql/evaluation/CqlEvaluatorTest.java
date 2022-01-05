/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ibm.cohort.cql.library.Format;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.parameters.IntegerParameter;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;

public class CqlEvaluatorTest {
    @Test
    public void testSetGetSuccess() {
        CqlTerminologyProvider terminologyProvider = mock(CqlTerminologyProvider.class);
        CqlDataProvider dataProvider = mock(CqlDataProvider.class); 
        CqlLibraryProvider libraryProvider = mock(CqlLibraryProvider.class);
        
        CqlEvaluator evaluator = new CqlEvaluator()
                .setTerminologyProvider(terminologyProvider)
                .setDataProvider(dataProvider)
                .setLibraryProvider(libraryProvider)
                .setCacheContexts(false);
        
        assertSame(terminologyProvider, evaluator.getTerminologyProvider());
        assertSame(dataProvider, evaluator.getDataProvider());
        assertSame(libraryProvider, evaluator.getLibraryProvider());
    }
    
    @Test
    public void testSimpleEvaluation() {
        CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor()
                .setLibraryId("Sample")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        CqlLibrary library = new CqlLibrary()
                .setDescriptor(libraryDescriptor)
                .setContent("library \"Sample\" version '1.0.0'\nparameter MinimumAge Integer\n\ndefine \"Something\":1<10\ndefine \"OtherThing\":10<1\ndefine EchoParam: MinimumAge");
        
        CqlTerminologyProvider terminologyProvider = mock(CqlTerminologyProvider.class);
        CqlDataProvider dataProvider = mock(CqlDataProvider.class); 
        
        CqlLibraryProvider libraryProvider = mock(CqlLibraryProvider.class);
        when(libraryProvider.getLibrary(libraryDescriptor)).thenReturn(library);
        
        CqlToElmTranslator translator = new CqlToElmTranslator();
        TranslatingCqlLibraryProvider translatingProvider = new TranslatingCqlLibraryProvider(libraryProvider, translator);
        
        CqlEvaluator evaluator = new CqlEvaluator()
                .setTerminologyProvider(terminologyProvider)
                .setDataProvider(dataProvider)
                .setLibraryProvider(translatingProvider)
                .setCacheContexts(false);
        
        int expectedMinimumAge = 17;
        
        Map<String,Parameter> parameters = new HashMap<>();
        parameters.put("MinimumAge", new IntegerParameter(expectedMinimumAge));
        
        Pair<String,String> context = Pair.of("Patient", "123");
        
        CqlEvaluationResult result = evaluator.evaluate(libraryDescriptor, parameters, context);
        assertNotNull(result);
        assertEquals(3, result.getExpressionResults().size());
        assertEquals(true, result.getExpressionResults().get("Something"));
        assertEquals(expectedMinimumAge, result.getExpressionResults().get("EchoParam"));
    }
    
    @Test
    public void testBatchEvaluation() {
        
        CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor()
                .setLibraryId("Sample")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        CqlLibrary library = new CqlLibrary()
                .setDescriptor(libraryDescriptor)
                .setContent("library \"Sample\" version '1.0.0'\nparameter MinimumAge Integer\n\ndefine \"Something\":1<10\ndefine \"OtherThing\":10<1\ndefine EchoParam: MinimumAge");
        
        CqlTerminologyProvider terminologyProvider = mock(CqlTerminologyProvider.class);
        CqlDataProvider dataProvider = mock(CqlDataProvider.class); 
        
        CqlLibraryProvider libraryProvider = mock(CqlLibraryProvider.class);
        when(libraryProvider.getLibrary(libraryDescriptor)).thenReturn(library);
        
        CqlToElmTranslator translator = new CqlToElmTranslator();
        TranslatingCqlLibraryProvider translatingProvider = new TranslatingCqlLibraryProvider(libraryProvider, translator);
        
        CqlEvaluator evaluator = new CqlEvaluator()
                .setTerminologyProvider(terminologyProvider)
                .setDataProvider(dataProvider)
                .setLibraryProvider(translatingProvider)
                .setCacheContexts(false);

        String parameterName = "MinimumAge";
        int expectedMinimumAge = 17;
        int expectedGlobalMinimumAge = expectedMinimumAge + 10;
        
        Map<String,Parameter> parameters = new HashMap<>();
        parameters.put(parameterName, new IntegerParameter(expectedMinimumAge));
        
        Pair<String,String> context = Pair.of("Patient", "123");

        CqlExpressionConfiguration expressionConfiguration = new CqlExpressionConfiguration();
        expressionConfiguration.setName("Something");

        CqlExpressionConfiguration expressionConfiguration2 = new CqlExpressionConfiguration();
        expressionConfiguration2.setName("EchoParam");
        
        CqlEvaluationRequest request = new CqlEvaluationRequest();
        request.setDescriptor(libraryDescriptor);
        request.setExpressions(Arrays.asList(expressionConfiguration, expressionConfiguration2).stream().collect(Collectors.toSet()));
        request.setParameters(parameters);
        request.setContextKey(context.getKey());
        request.setContextValue(context.getValue());
        
        CqlEvaluationRequests requests = new CqlEvaluationRequests();
        requests.setEvaluations(Arrays.asList(request));
        requests.setGlobalParameters(Collections.singletonMap(parameterName, new IntegerParameter(expectedGlobalMinimumAge)));
        
        // First do a full request with parameter override
        List<Pair<CqlEvaluationRequest,CqlEvaluationResult>> results = evaluator.evaluate(requests);
        assertEquals(1, results.size());
        CqlEvaluationResult result = results.get(0).getRight();
        assertNotNull(result);
        assertEquals(2, result.getExpressionResults().size());
        assertEquals(true, result.getExpressionResults().get("Something"));
        assertEquals(expectedMinimumAge, result.getExpressionResults().get("EchoParam"));
        
        // Now use the global parameter value instead
        request.getParameters().clear();
        results = evaluator.evaluate(requests);
        assertEquals(1, results.size());
        result = results.get(0).getRight();
        assertEquals(expectedGlobalMinimumAge, result.getExpressionResults().get("EchoParam"));
    }
    
    @Test
    public void testEvaluationExpressionSet() {
        CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor()
                .setLibraryId("Sample")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        CqlLibrary library = new CqlLibrary()
                .setDescriptor(libraryDescriptor)
                .setContent("library \"Sample\" version '1.0.0'\ndefine \"Something\":1<10\ndefine \"OtherThing\":10<1");
        
        CqlTerminologyProvider terminologyProvider = mock(CqlTerminologyProvider.class);
        CqlDataProvider dataProvider = mock(CqlDataProvider.class); 
        
        CqlLibraryProvider libraryProvider = mock(CqlLibraryProvider.class);
        when(libraryProvider.getLibrary(libraryDescriptor)).thenReturn(library);
        
        CqlToElmTranslator translator = new CqlToElmTranslator();
        TranslatingCqlLibraryProvider translatingProvider = new TranslatingCqlLibraryProvider(libraryProvider, translator);
        
        CqlEvaluator evaluator = new CqlEvaluator()
                .setTerminologyProvider(terminologyProvider)
                .setDataProvider(dataProvider)
                .setLibraryProvider(translatingProvider)
                .setCacheContexts(false);
        
        CqlEvaluationResult result = evaluator.evaluate(libraryDescriptor, null, new HashSet<>(Collections.singletonList("OtherThing")));
        assertNotNull(result);
        assertEquals(1, result.getExpressionResults().size());
        assertEquals(false, result.getExpressionResults().get("OtherThing"));
    }
}
