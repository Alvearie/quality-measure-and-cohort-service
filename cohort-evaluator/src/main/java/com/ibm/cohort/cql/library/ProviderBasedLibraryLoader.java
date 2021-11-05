/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.engine.elm.execution.OptimizedCqlLibraryReader;

public class ProviderBasedLibraryLoader implements LibraryLoader {

    private CqlLibraryProvider backingProvider;
    
    public ProviderBasedLibraryLoader(CqlLibraryProvider backingProvider) {
        this.backingProvider = backingProvider;
    }
    
    @Override
    public Library load(VersionedIdentifier vid) {
        CqlLibraryDescriptor desc = new CqlLibraryDescriptor()
                .setLibraryId( vid.getId() )
                .setVersion( vid.getVersion() )
                .setFormat( Format.ELM );        
        
        CqlLibrary cqlLibrary = backingProvider.getLibrary( desc );
        if( cqlLibrary != null ) {
            return deserializeLibrary(cqlLibrary);
        } else { 
            throw new IllegalArgumentException(String.format("Library '%s' version '%s' format '%s' not found", desc.getLibraryId(), desc.getVersion(), desc.getFormat().name()));
        }
    }

    /**
     * Inflate a serialized ELM library into the CQL executable artifact.
     * 
     * @param cqlLibrary CQL Library container
     * @return CQL engine executable artifact
     * @throws CqlLibraryDeserializationException on any deserialization error.
     */
    protected Library deserializeLibrary(CqlLibrary cqlLibrary) throws CqlLibraryDeserializationException {
        Library library;
        try { 
            library = OptimizedCqlLibraryReader.read(cqlLibrary.getContentAsStream());
        } catch( Exception ex ) {
            throw new CqlLibraryDeserializationException(ex);
        }
        return library;
    }
}
