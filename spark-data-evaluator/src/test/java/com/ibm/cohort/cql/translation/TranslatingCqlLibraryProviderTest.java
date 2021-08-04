package com.ibm.cohort.cql.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.fs.DirectoryBasedCqlLibraryProvider;

public class TranslatingCqlLibraryProviderTest {
    @Test
    public void testLoadWithTranslation() {
        CqlToElmTranslator translator = new CqlToElmTranslator();
        
        CqlLibraryProvider backingProvider = new DirectoryBasedCqlLibraryProvider(new File("src/test/resources/cql"));
        CqlLibraryProvider provider = new TranslatingCqlLibraryProvider(backingProvider, translator);
        
        assertEquals( backingProvider.listLibraries(), provider.listLibraries() );
        
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("CohortHelpers")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        CqlLibrary library = provider.getLibrary(descriptor);
        assertEquals( Format.ELM, library.getDescriptor().getFormat() );
        assertTrue( library.getContent().startsWith("<?xml") );
    }
}
