/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.Format;
import com.ibm.cohort.cql.provider.CqlLibrarySourceProvider;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.junit.Test;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;

public class CqlToElmTranslatorTest {
    @Test
    public void testTranslationWithErrors() {
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("GobbledyGook")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        CqlLibrary primaryLibrary = new CqlLibrary()
                .setDescriptor(descriptor)
                .setContent("GobbledyGook");
        
        CqlLibrarySourceProvider sourceProvider = vid -> primaryLibrary.getContentAsStream();
        
        CqlToElmTranslator translator = new CqlToElmTranslator();
        assertThrows( CqlTranslatorException.class, () -> translator.translate(primaryLibrary, sourceProvider) );
    }
    
    @Test
    public void testTranslationWithIncludes() throws Exception {
        CqlLibraryProvider provider = new ClasspathCqlLibraryProvider("cql");
        
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("SampleLibrary")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        CqlLibrary primaryLibrary = provider.getLibrary(descriptor);
        
        CqlLibrarySourceProvider sourceProvider = vid -> {
            CqlLibraryDescriptor d = new CqlLibraryDescriptor()
                    .setLibraryId(vid.getId())
                    .setVersion(vid.getVersion())
                    .setFormat(Format.CQL);
            
            return provider.getLibrary(d).getContentAsStream();
        };
        
        CqlToElmTranslator translator = new CqlToElmTranslator();
        try( Reader modelInfoXML = new FileReader(new File("src/test/resources/modelinfo/mock-modelinfo-1.0.0.xml") ) ) {
            translator.registerModelInfo(modelInfoXML);
        }
        CqlTranslationResult result = translator.translate(primaryLibrary, sourceProvider);
        assertEquals(1, result.getDependencies().size());
    }
}
