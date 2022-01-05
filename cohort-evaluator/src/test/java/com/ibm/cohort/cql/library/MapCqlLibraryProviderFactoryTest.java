/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;

public class MapCqlLibraryProviderFactoryTest {

    @Test
    public void fromDirectory() throws IOException {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory(new ZipStreamProcessor());
        CqlLibraryProvider provider = factory.fromDirectory(Paths.get("src/test/resources/cql/multiple-files"));
        assertTestIsPresent(provider);
        assertTestDateQueryIsPresent(provider);
    }

    @Test
    public void fromDirectory_withSearchPaths() throws IOException {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory(new ZipStreamProcessor());
        CqlLibraryProvider provider = factory.fromDirectory(
                Paths.get("src/test/resources/cql/multiple-files"),
                "sub-folder"
        );
        assertTestIsNotPresent(provider);
        assertTestDateQueryIsPresent(provider);
    }

    @Test
    public void fromZipFile() throws IOException {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory(new ZipStreamProcessor());
        CqlLibraryProvider provider = factory.fromZipFile(Paths.get("src/test/resources/cql/multiple-files/packaged-cqls.zip"));
        assertTestIsPresent(provider);
        assertTestDateQueryIsPresent(provider);
    }

    @Test
    public void fromZipFile_withSearchPaths() throws IOException {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory(new ZipStreamProcessor());
        CqlLibraryProvider provider = factory.fromZipFile(
                Paths.get("src/test/resources/cql/multiple-files/packaged-cqls.zip"),
               "sub-folder"
        );
        assertTestIsNotPresent(provider);
        assertTestDateQueryIsPresent(provider);
    }

    @Test
    public void fromZipStream() throws IOException {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory(new ZipStreamProcessor());
        try (InputStream is = this.getClass().getResourceAsStream("/cql/multiple-files/packaged-cqls.zip")) {
            CqlLibraryProvider provider = factory.fromZipStream(new ZipInputStream(is));
            assertTestIsPresent(provider);
            assertTestDateQueryIsPresent(provider);
        }
    }

    @Test
    public void fromZipStream_withSearchPaths() throws IOException {
        MapCqlLibraryProviderFactory factory = new MapCqlLibraryProviderFactory(new ZipStreamProcessor());
        try (InputStream is = this.getClass().getResourceAsStream("/cql/multiple-files/packaged-cqls.zip")) {
            CqlLibraryProvider provider = factory.fromZipStream(new ZipInputStream(is), "sub-folder");
            assertTestIsNotPresent(provider);
            assertTestDateQueryIsPresent(provider);
        }
    }


    private void assertTestIsPresent(CqlLibraryProvider provider) {
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("Test")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        CqlLibrary library = provider.getLibrary(descriptor);
        Assert.assertEquals(descriptor, library.getDescriptor());
        Assert.assertTrue(library.getContent().startsWith("library \"Test\" version '1.0.0'"));
    }

    private void assertTestIsNotPresent(CqlLibraryProvider provider) {
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("Test")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        Assert.assertNull(provider.getLibrary(descriptor));
    }

    private void assertTestDateQueryIsPresent(CqlLibraryProvider provider) {
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("TestDateQuery")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        CqlLibrary library = provider.getLibrary(descriptor);
        Assert.assertEquals(descriptor, library.getDescriptor());
        Assert.assertTrue(library.getContent().startsWith("library \"TestDateQuery\" version '1.0.0'"));
    }

}
