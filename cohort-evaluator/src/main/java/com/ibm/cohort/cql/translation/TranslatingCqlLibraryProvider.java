/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.Format;
import com.ibm.cohort.cql.provider.ProviderBasedCqlLibrarySourceProvider;

public class TranslatingCqlLibraryProvider implements CqlLibraryProvider {
    private final CqlLibraryProvider backingLibraryProvider;
    private final CqlToElmTranslator translator;
    private final boolean forceTranslation;
    
    // Cache of libraries that have already been translated
    private final Map<CqlLibraryDescriptor, CqlLibrary> translations = new HashMap<>();
    
    public TranslatingCqlLibraryProvider(CqlLibraryProvider backingProvider, CqlToElmTranslator translator) {
        this(backingProvider, translator, false);
    }

    public TranslatingCqlLibraryProvider(CqlLibraryProvider backingLibraryProvider, CqlToElmTranslator translator, boolean forceTranslation) {
        this.backingLibraryProvider = backingLibraryProvider;
        this.translator = translator;
        this.forceTranslation = forceTranslation;
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
                if( library == null || forceTranslation ) {
                    CqlLibraryDescriptor cqlDescriptor = new CqlLibraryDescriptor()
                            .setLibraryId( key.getLibraryId() )
                            .setVersion( key.getVersion() )
                            .setFormat(Format.CQL);
                    
                    library = backingLibraryProvider.getLibrary(cqlDescriptor);
                    if( library != null ) {
                        CqlTranslationResult translationResult = translator.translate(library, new ProviderBasedCqlLibrarySourceProvider(backingLibraryProvider));
                        return translationResult.getMainLibrary();
                    }
                }
                return library;
            });
        }
    }
}
