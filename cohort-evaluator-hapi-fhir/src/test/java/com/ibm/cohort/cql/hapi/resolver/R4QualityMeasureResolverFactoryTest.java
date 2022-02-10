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
import org.apache.commons.collections.ListUtils;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipInputStream;

public class R4QualityMeasureResolverFactoryTest {

    private final List<String> allLibraryIds = Arrays.asList(
            "Library/Library1-id",
            "Library/Library2-id",
            "Library/Library3-id",
            "Library/Library4-id",
            "Library/Library5-id",
            "Library/Library-NoId"
    );
    private final List<String> allMeasureIds = Arrays.asList(
            "Measure/Measure1-id",
            "Measure/Measure2-id",
            "Measure/Measure3-id"
    );
    private R4QualityMeasureResolverFactory factory;

    @Before
    public void setup() {
        if (factory == null) {
            FhirContext context = FhirContext.forR4();
            IParser parser = context.newJsonParser();
            factory = new R4QualityMeasureResolverFactory(parser, new ZipStreamProcessor());
        }
    }

    @Test
    public void fromDirectory_noSearchPaths() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test");
        R4QualityMeasureResolvers resolvers = factory.fromDirectory(directory);

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library1-id",
                "Library/Library2-id",
                "Library/Library3-id",
                "Library/Library4-id",
                "Library/Library5-id",
                "Library/Library-NoId"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver(),
                "Measure/Measure1-id",
                "Measure/Measure2-id",
                "Measure/Measure3-id"
        );
    }

    @Test
    public void fromDirectory_searchPath_subFolder() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test");
        R4QualityMeasureResolvers resolvers = factory.fromDirectory(directory, "sub-folder");

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library3-id",
                "Library/Library4-id"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver(),
                "Measure/Measure3-id"
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
        R4QualityMeasureResolvers resolvers = factory.fromDirectory(directory, searchPath.toString());

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library5-id"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver()
        );
    }

    @Test
    public void fromZipFile_noSearchPaths() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test/zipped-libraries.zip");
        R4QualityMeasureResolvers resolvers = factory.fromZipFile(directory);

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library1-id",
                "Library/Library2-id",
                "Library/Library3-id",
                "Library/Library4-id",
                "Library/Library5-id",
                "Library/Library-NoId"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver(),
                "Measure/Measure1-id",
                "Measure/Measure2-id",
                "Measure/Measure3-id"
        );
    }

    @Test
    public void fromZipFile_searchPath_subFolder() throws IOException {
        Path directory = Paths.get("src/test/resources/fhir/file-test/zipped-libraries.zip");
        R4QualityMeasureResolvers resolvers = factory.fromZipFile(directory, "sub-folder");

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library3-id",
                "Library/Library4-id"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver(),
                "Measure/Measure3-id"
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
        R4QualityMeasureResolvers resolvers = factory.fromZipFile(directory, "sub-folder/subsub-folder");

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library5-id"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver()
        );
    }

    @Test
    public void fromZipStream_noSearchPaths() throws IOException {
        R4QualityMeasureResolvers resolvers;
        try (InputStream is = this.getClass().getResourceAsStream("/fhir/file-test/zipped-libraries.zip")) {
            resolvers = factory.fromZipStream(new ZipInputStream(is));
        }

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library1-id",
                "Library/Library2-id",
                "Library/Library3-id",
                "Library/Library4-id",
                "Library/Library5-id",
                "Library/Library-NoId"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver(),
                "Measure/Measure1-id",
                "Measure/Measure2-id",
                "Measure/Measure3-id"
        );
    }

    @Test
    public void fromZipStream_searchPath_subFolder() throws IOException {
        R4QualityMeasureResolvers resolvers;
        try (InputStream is = this.getClass().getResourceAsStream("/fhir/file-test/zipped-libraries.zip")) {
            resolvers = factory.fromZipStream(new ZipInputStream(is), "sub-folder");
        }

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library3-id",
                "Library/Library4-id"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver(),
                "Measure/Measure3-id"
        );
    }

    @Test
    public void fromZipStream_searchPath_subSubFolder() throws IOException {
        R4QualityMeasureResolvers resolvers;
        try (InputStream is = this.getClass().getResourceAsStream("/fhir/file-test/zipped-libraries.zip")) {
            // See `fromZipFile_searchPath_subSubFolder()` for more information on the searchPaths used here.
            resolvers = factory.fromZipStream(new ZipInputStream(is), "sub-folder/subsub-folder");
        }

        assertLibraryIdsArePresent(
                resolvers.getLibraryResolver(),
                "Library/Library5-id"
        );

        assertMeasureIdsArePresent(
                resolvers.getMeasureResolver()
        );
    }

    private void assertLibraryIdsArePresent(FhirResourceResolver<Library> resolver, String... ids) {
        assertIdsArePresent(allLibraryIds, ids, x -> {
            Library lib = resolver.resolveById(x);
            return lib == null ? null : lib.getId();
        });
    }

    private void assertMeasureIdsArePresent(FhirResourceResolver<Measure> resolver, String... ids) {
        assertIdsArePresent(allMeasureIds, ids, x -> {
            Measure measure = resolver.resolveById(x);
            return measure == null ? null : measure.getId();
        });
    }

    private void assertIdsArePresent(List<String> allIds, String[] ids, Function<String, String> idFunction) {
        List<String> expectedIds = Arrays.asList(ids);
        List<String> unexpectedIds = new ArrayList<>(allIds);
        unexpectedIds.removeAll(expectedIds);
        for (String id : expectedIds) {
            String actualId = idFunction.apply(id);
            Assert.assertNotNull(id + " not found", actualId);
            Assert.assertEquals(id, actualId);
        }
        for (String id : unexpectedIds) {
            String actualId = idFunction.apply(id);
            Assert.assertNull(id + " unexpectedly found", actualId);
        }
    }

}
