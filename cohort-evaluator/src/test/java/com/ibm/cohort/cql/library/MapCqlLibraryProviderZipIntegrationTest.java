/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.library;

import java.nio.file.Paths;

import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.junit.Assert;
import org.junit.Test;

import org.opencds.cqf.cql.engine.execution.LibraryLoader;

public class MapCqlLibraryProviderZipIntegrationTest {

    @Test
    public void testLibraryFoundInZipSuccess() throws Exception {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory();
        CqlLibraryProvider provider = factory.fromZipFile(Paths.get("src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip"));
        CqlLibraryProvider fhirClasspathProvider = new ClasspathCqlLibraryProvider();
        provider = new PriorityCqlLibraryProvider(provider, fhirClasspathProvider);
        CqlToElmTranslator translator = new CqlToElmTranslator();
        provider = new TranslatingCqlLibraryProvider(provider, translator, false);
        LibraryLoader libraryLoader = new ProviderBasedLibraryLoader(provider);

        VersionedIdentifier id = new VersionedIdentifier()
                .withId("BreastCancerScreening");

        Library library = libraryLoader.load(id);
        Assert.assertEquals("BreastCancerScreening", library.getIdentifier().getId());
    }

    @Test
    public void testLibraryFoundInZipWithSearchPathsSuccess() throws Exception {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory();
        CqlLibraryProvider provider = factory.fromZipFile(
                Paths.get("src/test/resources/cql/zip-structured/col_colorectal_cancer_screening_v1_0_0.zip"),
                "CDSexport"
        );
        CqlLibraryProvider fhirClasspathProvider = new ClasspathCqlLibraryProvider();
        provider = new PriorityCqlLibraryProvider(provider, fhirClasspathProvider);
        CqlToElmTranslator translator = new CqlToElmTranslator();
        provider = new TranslatingCqlLibraryProvider(provider, translator, false);
        LibraryLoader libraryLoader = new ProviderBasedLibraryLoader(provider);

        VersionedIdentifier id = new VersionedIdentifier()
                .withId("COL_InitialPop")
                .withVersion("1.0.0");

        Library library = libraryLoader.load(id);
        Assert.assertEquals("COL_InitialPop", library.getIdentifier().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLibraryFoundInZipWithSearchPathsMissingError() throws Exception {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory();
        CqlLibraryProvider provider = factory.fromZipFile(
                Paths.get("src/test/resources/cql/zip-structured/col_colorectal_cancer_screening_v1_0_0.zip"),
                "deploypackage"
        );
        CqlLibraryProvider fhirClasspathProvider = new ClasspathCqlLibraryProvider();
        provider = new PriorityCqlLibraryProvider(provider, fhirClasspathProvider);
        CqlToElmTranslator translator = new CqlToElmTranslator();
        provider = new TranslatingCqlLibraryProvider(provider, translator, false);
        LibraryLoader libraryLoader = new ProviderBasedLibraryLoader(provider);

        VersionedIdentifier id = new VersionedIdentifier()
                .withId("COL_InitialPop")
                .withVersion("1.0.0");

        libraryLoader.load(id);
    }

}
