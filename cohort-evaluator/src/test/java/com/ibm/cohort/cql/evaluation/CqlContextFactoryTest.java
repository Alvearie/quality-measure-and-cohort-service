package com.ibm.cohort.cql.evaluation;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.opencds.cqf.cql.engine.execution.Context;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.CqlContextFactory.ContextCacheKey;
import com.ibm.cohort.cql.evaluation.parameters.IntegerParameter;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.evaluation.parameters.StringParameter;
import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.library.PriorityCqlLibraryProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import com.ibm.cohort.cql.terminology.UnsupportedTerminologyProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;

public class CqlContextFactoryTest {
    @Test
    public void testCreateContextSuccess() {
        boolean expectedDebug = true;
        ZonedDateTime expectedEvaluationDateTime = ZonedDateTime.of(LocalDateTime.of(2001, 10, 2, 11, 12, 13), ZoneId.of("America/New_York"));

        PriorityCqlLibraryProvider libraryProvider = new PriorityCqlLibraryProvider( new DirectoryBasedCqlLibraryProvider( new File("src/test/resources/cql") ), new ClasspathCqlLibraryProvider("org.hl7.fhir") );

        CqlToElmTranslator translator = new CqlToElmTranslator();
        TranslatingCqlLibraryProvider translatingProvider = new TranslatingCqlLibraryProvider(libraryProvider, translator);
        
        CqlLibraryDescriptor topLevelLibrary = new CqlLibraryDescriptor()
                .setLibraryId("MyCQL")
                .setVersion("1.0.0")
                .setFormat(Format.ELM);
        
        CqlTerminologyProvider terminologyProvider = new UnsupportedTerminologyProvider();
        
        CqlDataProvider dataProvider = mock(CqlDataProvider.class);
        
        Pair<String,String> contextData = Pair.of("Patient", "123");
        
        Map<String,Parameter> expectedParams = new HashMap<>();
        expectedParams.put("P1", new StringParameter("MyString"));
        expectedParams.put("P2", new IntegerParameter(10));
        
        CqlContextFactory cqlContextFactory = spy(CqlContextFactory.class);
        
        Context context = cqlContextFactory.createContext(translatingProvider, topLevelLibrary, terminologyProvider,
                dataProvider, expectedEvaluationDateTime, contextData, expectedParams,
                expectedDebug ? CqlDebug.DEBUG : CqlDebug.NONE);
        
        assertEquals(expectedDebug, context.getDebugMap().getIsLoggingEnabled());
        assertEquals(expectedEvaluationDateTime.toInstant(), context.getEvaluationDateTime().getDateTime().toZonedDateTime().toInstant());
        context.enterContext(contextData.getKey());
        assertEquals(contextData.getValue(), context.getCurrentContextValue());
        assertEquals(topLevelLibrary.getLibraryId(), context.getCurrentLibrary().getIdentifier().getId());
        assertEquals(topLevelLibrary.getVersion(), context.getCurrentLibrary().getIdentifier().getVersion());
        
        for( Map.Entry<String, Parameter> entry : expectedParams.entrySet() ) {
            Object actualValue = context.resolveParameterRef(null, entry.getKey());
            assertEquals( entry.getValue().toCqlType(), actualValue );
        }
        
        // Once more just to check that caching is working. Using a different data provider because that is
        // how we will actually use it at runtime.
        CqlDataProvider dataProvider2 = mock(CqlDataProvider.class);
        cqlContextFactory.createContext(translatingProvider, topLevelLibrary, terminologyProvider,
                dataProvider2, expectedEvaluationDateTime, contextData, expectedParams,
                expectedDebug ? CqlDebug.DEBUG : CqlDebug.NONE);
        
        verify(cqlContextFactory, times(1)).createContext(any(ContextCacheKey.class));
    }
    
    @Test
    public void testContextCacheKeyEquals() {
        CqlLibraryProvider libraryProvider = mock(CqlLibraryProvider.class);
        CqlTerminologyProvider terminologyProvider = mock(CqlTerminologyProvider.class);
        
        CqlLibraryDescriptor topLevelLibrary = new CqlLibraryDescriptor().setLibraryId("Test").setVersion("1.0.0").setFormat(Format.ELM);
        
        Map<String,Parameter> parameters = new HashMap<>();
        ZonedDateTime evaluationDateTime = ZonedDateTime.now();
        
        
        CqlContextFactory.ContextCacheKey k1 = new CqlContextFactory.ContextCacheKey(libraryProvider, topLevelLibrary, terminologyProvider, evaluationDateTime, parameters);
        assertEquals(k1, k1);
        
        CqlContextFactory.ContextCacheKey k2 = new CqlContextFactory.ContextCacheKey(libraryProvider, topLevelLibrary, terminologyProvider, evaluationDateTime, parameters);
        assertEquals(k1, k2);
        
        Map<ContextCacheKey,String> map = new HashMap<>();
        map.put(k1, "Hello,World");
        
        assertEquals( "Hello,World", map.get(k2) );
    }
}
