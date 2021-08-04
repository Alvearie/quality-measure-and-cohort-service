package com.ibm.cohort.cql.evaluation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.opencds.cqf.cql.engine.execution.Context;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.fs.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.spark.data.DataRowDataProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import com.ibm.cohort.cql.terminology.UnsupportedTerminologyProvider;
import com.ibm.cohort.datarow.engine.DataRowRetrieveProvider;

public class CqlContextFactoryTest {
    @Test
    public void testCreateContextSuccess() {
        boolean expectedDebug = true;
        ZonedDateTime expectedEvaluationDateTime = ZonedDateTime.of(LocalDateTime.of(2001, 10, 2, 11, 12, 13), ZoneId.of("America/New_York"));
        
        CqlLibraryProvider libraryProvider = new DirectoryBasedCqlLibraryProvider( new File("src/test/resources/cql") );
        
        CqlLibraryDescriptor topLevelLibrary = new CqlLibraryDescriptor()
                .setLibraryId("MyCQL")
                .setVersion("1.0.0")
                .setFormat(Format.ELM);
        
        CqlTerminologyProvider terminologyProvider = new UnsupportedTerminologyProvider();
        
        DataRowRetrieveProvider retrieveProvider = mock(DataRowRetrieveProvider.class);
        
        CqlDataProvider dataProvider = new DataRowDataProvider(retrieveProvider);
        
        Context context = new CqlContextFactory()
                .setDebug(true)
                .setEvaluationDateTime(expectedEvaluationDateTime)
                .createContext(libraryProvider, topLevelLibrary, terminologyProvider, dataProvider);
        
        assertEquals(expectedDebug, context.getDebugMap().getIsLoggingEnabled());
        assertEquals(expectedEvaluationDateTime.toInstant(), context.getEvaluationDateTime().getDateTime().toZonedDateTime().toInstant());
        assertEquals(topLevelLibrary.getLibraryId(), context.getCurrentLibrary().getIdentifier().getId());
        assertEquals(topLevelLibrary.getVersion(), context.getCurrentLibrary().getIdentifier().getVersion());
    }
}
