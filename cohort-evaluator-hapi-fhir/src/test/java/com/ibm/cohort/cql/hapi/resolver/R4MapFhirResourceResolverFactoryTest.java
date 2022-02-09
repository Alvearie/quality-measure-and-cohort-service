/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.ibm.cohort.cql.fhir.handler.ResourceHandler;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.handler.R4LibraryResourceHandler;
import com.ibm.cohort.cql.library.ZipStreamProcessor;
import com.ibm.cohort.cql.version.ResourceSelector;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;

public class R4MapFhirResourceResolverFactoryTest {

    private R4MapFhirResourceResolverFactory<Library> factory;

    @Before
    public void setup() {
        if (factory == null) {
            FhirContext context = FhirContext.forR4();
            IParser parser = context.newJsonParser();

            ResourceHandler<Library, Identifier> resourceHandler = new R4LibraryResourceHandler();
            ResourceSelector<Library> resourceSelector = new ResourceSelector<>(resourceHandler);
            ZipStreamProcessor zipProcessor = new ZipStreamProcessor();

            factory = new R4MapFhirResourceResolverFactory<>(
                    resourceHandler,
                    resourceSelector,
                    parser,
                    zipProcessor
            );
        }
    }

    @Test
    public void fromDirectory_noSearchPaths() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test");
        FhirResourceResolver<Library> resolver = factory.fromDirectory(directory);

        assertIdsArePresent(
                resolver,
                "Library/Library1-id",
                "Library/Library2-id",
                "Library/Library3-id",
                "Library/Library4-id",
                "Library/Library5-id",
                "Library/Library-NoId"
        );
    }

    @Test
    public void fromDirectory_searchPath_subFolder() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test");
        FhirResourceResolver<Library> resolver = factory.fromDirectory(directory, "sub-folder");

        assertIdsArePresent(
                resolver,
                "Library/Library3-id",
                "Library/Library4-id"
        );
    }

    @Test
    public void fromDirectory_searchPath_subSubFolder() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test");
        /* Note the use of `Paths.get()` for the searchPath.
         * Java will change the path separator to the OS default when resolving down to a string.
         * This allows the test to run on Unix and Windows machines without any code changes.
         *
         * See `fromZipFile_searchPath_subSubFolder()` for how we handle this
         * usecase with zip files.
         */
        Path searchPath = Paths.get("sub-folder", "subsub-folder");
        FhirResourceResolver<Library> resolver = factory.fromDirectory(directory, searchPath.toString());

        assertIdsArePresent(
                resolver,
                "Library/Library5-id"
        );
    }

    @Test
    public void fromZipFile_noSearchPaths() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test/zipped-libraries.zip");
        FhirResourceResolver<Library> resolver = factory.fromZipFile(directory);

        assertIdsArePresent(
                resolver,
                "Library/Library1-id",
                "Library/Library2-id",
                "Library/Library3-id",
                "Library/Library4-id",
                "Library/Library5-id",
                "Library/Library-NoId"
        );
    }

    @Test
    public void fromZipFile_searchPath_subFolder() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test/zipped-libraries.zip");
        FhirResourceResolver<Library> resolver = factory.fromZipFile(directory, "sub-folder");

        assertIdsArePresent(
                resolver,
                "Library/Library3-id",
                "Library/Library4-id"
        );
    }

    @Test
    public void fromZipFile_searchPath_subSubFolder() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test/zipped-libraries.zip");
        /* Note the hard-coded use of `/` here as the searchPath separator.
         * Even though Unix and Windows have different path separators, both
         * OSes will read the paths from inside the zip file with a `/`.
         * I believe this is tied to the fact that the zip file was created
         * in a Mac (Unix) environment.
         *
         * See `fromDirectory_searchPath_subSubFolder()` for how we handle
         * this usecase with directories.
         */
        FhirResourceResolver<Library> resolver = factory.fromZipFile(directory, "sub-folder/subsub-folder");

        assertIdsArePresent(
                resolver,
                "Library/Library5-id"
        );
    }

    @Test
    public void fromZipStream_noSearchPaths() throws IOException {
        FhirResourceResolver<Library> resolver;
        try (InputStream is = this.getClass().getResourceAsStream("/fhir/file-test/zipped-libraries.zip")) {
            resolver = factory.fromZipStream(new ZipInputStream(is));
        }

        assertIdsArePresent(
                resolver,
                "Library/Library1-id",
                "Library/Library2-id",
                "Library/Library3-id",
                "Library/Library4-id",
                "Library/Library5-id",
                "Library/Library-NoId"
        );
    }

    @Test
    public void fromZipStream_searchPath_subFolder() throws IOException {
        FhirResourceResolver<Library> resolver;
        try (InputStream is = this.getClass().getResourceAsStream("/fhir/file-test/zipped-libraries.zip")) {
            resolver = factory.fromZipStream(new ZipInputStream(is));
        }

        assertIdsArePresent(
                resolver,
                "Library/Library3-id",
                "Library/Library4-id"
        );
    }

    @Test
    public void fromZipStream_searchPath_subSubFolder() throws IOException {
        FhirResourceResolver<Library> resolver;
        try (InputStream is = this.getClass().getResourceAsStream("/fhir/file-test/zipped-libraries.zip")) {
            resolver = factory.fromZipStream(new ZipInputStream(is));
        }

        assertIdsArePresent(
                resolver,
                "Library/Library5-id"
        );
    }

    private void assertIdsArePresent(FhirResourceResolver<Library> resolver, String... ids) {
        for (String id : ids) {
            Library library = resolver.resolveById(id);
            Assert.assertNotNull(id + " not found", library);
            Assert.assertEquals(id, library.getId());
        }
    }

}
