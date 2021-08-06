package com.ibm.cohort.cql.library;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.junit.Test;

public class ProviderBasedLibraryLoaderTest {
    @Test
    public void libraryNotFoundThrowsException() {
        CqlLibraryProvider provider = mock(CqlLibraryProvider.class);
        
        ProviderBasedLibraryLoader loader = new ProviderBasedLibraryLoader(provider);
        assertThrows(IllegalArgumentException.class, () -> loader.load(new VersionedIdentifier().withId("Name").withVersion("1.0.0")));
    }
}
