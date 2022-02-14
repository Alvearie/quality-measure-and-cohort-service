/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.Format;
import com.ibm.cohort.cql.library.MapCqlLibraryProviderFactory;
import com.ibm.cohort.cql.library.ProviderBasedLibraryLoader;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

public class CqlTranslationIntegrationTest {

    @Test
    public void multipleFilesInZip__translatedSuccessfully() throws Exception {
        Path zipFile = Paths.get("src/test/resources/cql/multiple-files/packaged-cqls.zip");

        MapCqlLibraryProviderFactory providerFactory = new MapCqlLibraryProviderFactory();
        CqlLibraryProvider backingLibraryProvider = providerFactory.fromZipFile(zipFile);
        runMultipleFilesTest(backingLibraryProvider);
    }

    @Test
    public void multipleFilesInFolder__translatedSuccessfully() throws Exception {
        Path rootDirectory = Paths.get("src/test/resources/cql/multiple-files");

        MapCqlLibraryProviderFactory providerFactory = new MapCqlLibraryProviderFactory();
        CqlLibraryProvider backingLibraryProvider = providerFactory.fromDirectory(rootDirectory);
        runMultipleFilesTest(backingLibraryProvider);
    }

    @Test
    public void singleFile_withOptions__translatedSuccessfully() {
        CqlLibraryProvider backingLibraryProvider = new ClasspathCqlLibraryProvider("cql.basic");
        CqlToElmTranslator translator = new CqlToElmTranslator();
        Path modelInfo = Paths.get("src/test/resources/modelinfo/ig-with-target-modelinfo-0.0.1.xml");
        translator.registerModelInfo(modelInfo.toFile());
        CqlLibraryProvider translatingLibraryProvider = new TranslatingCqlLibraryProvider(backingLibraryProvider, translator, true);
        LibraryLoader libraryLoader = new ProviderBasedLibraryLoader(translatingLibraryProvider);

        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId("Test")
                .withVersion("1.0.0");
        Library library = libraryLoader.load(identifier);

        Assert.assertEquals(identifier, library.getIdentifier());
        Assert.assertEquals(2, library.getAnnotation().size());
        Assert.assertEquals(4, library.getStatements().getDef().size());
    }

    @Test
    public void errorCausedByInvalidContent() {
        CqlLibraryProvider backingLibraryProvider = cqlResourceDescriptor -> {
            CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                    .setLibraryId("Junk")
                    .setLibraryId("1.0.0")
                    .setFormat(Format.CQL);
            String content = "this is a junk cql";
            return new CqlLibrary()
                    .setContent(content)
                    .setDescriptor(descriptor);
        };
        CqlToElmTranslator translator = new CqlToElmTranslator();
        CqlLibraryProvider translatingLibraryProvider = new TranslatingCqlLibraryProvider(backingLibraryProvider, translator, true);

        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("Junk")
                .setLibraryId("1.0.0")
                .setFormat(Format.ELM);
        boolean failed = false;
        try {
            translatingLibraryProvider.getLibrary(descriptor);
        }
        catch (Exception e) {
            failed = true;
            Assert.assertTrue(
                    "Unexpected exception message: " + e.getMessage(),
                    e.getMessage().startsWith("There were errors during cql translation:")
            );
        }
        if (!failed) {
            Assert.fail("Did not fail translation");
        }
    }

    @Test
    public void exceptionCausedByNotIncludingFHIRHelpers() {
        CqlLibraryProvider backingLibraryProvider = new ClasspathCqlLibraryProvider("cql.failure");
        CqlToElmTranslator translator = new CqlToElmTranslator();
        CqlLibraryProvider translatingLibraryProvider = new TranslatingCqlLibraryProvider(backingLibraryProvider, translator, true);

        boolean failed = false;
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("MissingFhirHelpers")
                .setVersion("1.0.0")
                .setFormat(Format.ELM);
        try {
            translatingLibraryProvider.getLibrary(descriptor);
        }
        catch (Exception e) {
            failed = true;
            Assert.assertTrue(
                    "Unexpected exception message: " + e.getMessage(),
                    e.getMessage().startsWith("There were exceptions during cql translation:")
            );
        }
        if (!failed) {
            Assert.fail("Did not fail translation");
        }
    }

    private void runMultipleFilesTest(CqlLibraryProvider backingLibraryProvider) {
        Path modelInfo = Paths.get("src/test/resources/modelinfo/ig-with-target-modelinfo-0.0.1.xml");

        CqlToElmTranslator translator = new CqlToElmTranslator();
        translator.registerModelInfo(modelInfo.toFile());
        CqlLibraryProvider libraryProvider = new TranslatingCqlLibraryProvider(backingLibraryProvider, translator, true);

        List<String> libraryNames = Arrays.asList("BreastCancerScreening", "Test", "TestDateQuery");
        for (String libraryName : libraryNames) {
            CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                    .setLibraryId(libraryName)
                    .setVersion("1.0.0")
                    .setFormat(Format.ELM);
            CqlLibrary library = libraryProvider.getLibrary(descriptor);
            Assert.assertFalse(library.getContent().isEmpty());
        }
    }
}
