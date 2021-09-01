package com.ibm.cohort.cql.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;

public class DirectoryBasedCqlLibraryProviderTest {
    @Test
    public void testListAndRetrieveSuccess() {
        File directory = new File("src/test/resources/cql");
        
        CqlLibraryProvider provider = new DirectoryBasedCqlLibraryProvider(directory);
        Collection<CqlLibraryDescriptor> libraries = provider.listLibraries();
        assertEquals(4, libraries.size());
        
        CqlLibraryDescriptor expectedCql = new CqlLibraryDescriptor()
                .setLibraryId("CohortHelpers")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        assertTrue("Missing expected library", libraries.contains(expectedCql));
        
        CqlLibraryDescriptor expectedElm = new CqlLibraryDescriptor()
                .setLibraryId("MyCQL")
                .setVersion("1.0.0")
                .setFormat(Format.ELM);
        
        assertTrue("Missing expected library", libraries.contains(expectedElm));
    }
}
