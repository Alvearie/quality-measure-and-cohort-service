/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;

public class TranslatingCqlLibraryProvider implements CqlLibraryProvider {
    private final CqlLibraryProvider backingLibraryProvider;
    private final CqlToElmTranslator translator;
    
    // Cache of libraries that have already been translated
    private Map<CqlLibraryDescriptor, CqlLibrary> translations;
    
    public TranslatingCqlLibraryProvider(CqlLibraryProvider backingProvider, CqlToElmTranslator translator) {
        this.backingLibraryProvider = backingProvider;
        this.translator = translator;
        this.translations = new HashMap<>();
    }
    
    @Override
    public Collection<CqlLibraryDescriptor> listLibraries() {
        return backingLibraryProvider.listLibraries();
    }
    
    @Override
    public CqlLibrary getLibrary(CqlLibraryDescriptor libraryDescriptor) {
        if( libraryDescriptor.getFormat().equals(Format.CQL) ) {
            return backingLibraryProvider.getLibrary(libraryDescriptor);
        } else {
            return translations.computeIfAbsent( libraryDescriptor, key -> {
                CqlLibraryDescriptor elmDescriptor = new CqlLibraryDescriptor()
                        .setLibraryId(key.getLibraryId())
                        .setVersion(key.getVersion())
                        .setFormat(Format.ELM);
                
                CqlLibrary library = backingLibraryProvider.getLibrary(elmDescriptor);
                if( library == null ) {
                    CqlLibraryDescriptor cqlDescriptor = new CqlLibraryDescriptor()
                            .setLibraryId( key.getLibraryId() )
                            .setVersion( key.getVersion() )
                            .setFormat( CqlLibraryDescriptor.Format.CQL );
                    
                    library = backingLibraryProvider.getLibrary(cqlDescriptor);
                    if( library != null ) {
                        CqlTranslationResult translationResult = translator.translate(library, new DefaultCqlLibrarySourceProvider(backingLibraryProvider));
                        return translationResult.getMainLibrary();
                    }
                }
                return library;
            });
        }
    }
}
