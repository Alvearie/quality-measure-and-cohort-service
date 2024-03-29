/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.Reader;

import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.Format;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.junit.Test;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;

public class TranslatingCqlLibraryProviderTest {
    @Test
    public void testLoadWithTranslation() throws Exception {
        CqlToElmTranslator translator = new CqlToElmTranslator();
        try( Reader modelInfoXML = new FileReader("src/test/resources/modelinfo/mock-modelinfo-1.0.0.xml") ) {
            translator.registerModelInfo(modelInfoXML);
        }

        CqlLibraryProvider backingProvider = new ClasspathCqlLibraryProvider("cql");
        CqlLibraryProvider provider = new TranslatingCqlLibraryProvider(backingProvider, translator);
        
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("CohortHelpers")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        CqlLibrary library = provider.getLibrary(descriptor);
        assertEquals( Format.CQL, library.getDescriptor().getFormat() );
        assertTrue( library.getContent().startsWith("library") );
        
        descriptor.setFormat(Format.ELM);
        library = provider.getLibrary(descriptor);
        assertEquals( Format.ELM, library.getDescriptor().getFormat() );
        assertTrue( library.getContent().startsWith("<?xml") );
    }

    @Test
    public void testTranslationWithNullCqlContexts() {
        CqlLibraryProvider backingProvider = new ClasspathCqlLibraryProvider("cql/empty-context");
        CqlLibraryProvider provider = new TranslatingCqlLibraryProvider(backingProvider, new CqlToElmTranslator());

        CqlLibraryDescriptor emptyContext = new CqlLibraryDescriptor()
            .setLibraryId("EmptyContext")
            .setVersion("1.0.0")
            .setFormat(Format.ELM);
        Exception exception = assertThrows(CqlTranslatorException.class, () -> provider.getLibrary(emptyContext));
        assertThat(exception.getMessage(), containsString("must specify a Context"));

        CqlLibraryDescriptor emptyHelper = new CqlLibraryDescriptor()
            .setLibraryId("EmptyHelper")
            .setVersion("1.0.0")
            .setFormat(Format.ELM);
        CqlLibrary library = provider.getLibrary(emptyHelper);
        assertThat(library.getContent(), not(isEmptyOrNullString()));
    }
}
