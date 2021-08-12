package com.ibm.cohort.cql.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
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
                .setLibraryProvider(libraryProvider);
        
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
                .setLibraryProvider(translatingProvider);
        
        CqlEvaluationResult result = evaluator.evaluate(libraryDescriptor);
        assertNotNull(result);
        assertEquals(2, result.getExpressionResults().size());
        assertEquals(true, result.getExpressionResults().get("Something"));
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
                .setLibraryProvider(translatingProvider);
        
        CqlEvaluationResult result = evaluator.evaluate(libraryDescriptor, null, new HashSet<>(Collections.singletonList("OtherThing")));
        assertNotNull(result);
        assertEquals(1, result.getExpressionResults().size());
        assertEquals(false, result.getExpressionResults().get("OtherThing"));
    }
}
