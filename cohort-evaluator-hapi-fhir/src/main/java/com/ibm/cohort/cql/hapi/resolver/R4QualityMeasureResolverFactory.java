/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.parser.IParser;
import com.ibm.cohort.cql.hapi.handler.R4LibraryResourceFieldHandler;
import com.ibm.cohort.cql.hapi.handler.R4MeasureResourceFieldHandler;
import com.ibm.cohort.cql.version.ResourceSelector;
import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import com.ibm.cohort.cql.fhir.resolver.MapFhirResourceResolver;
import com.ibm.cohort.cql.hapi.HapiUtils;
import com.ibm.cohort.cql.helpers.PathHelper;
import com.ibm.cohort.cql.library.ZipStreamProcessor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

/**
 * A purpose-built factory for creating a suite of {@link MapFhirResourceResolver}s
 * derived from arbitrary packaged form factors (e.g. directory, zip, etc).
 *
 * <p> All resolvers are returned together via a {@link R4QualityMeasureResolvers}
 * instance.  This allows the factory to only traverse the packaged content
 * once rather than once per data type.
 */
public class R4QualityMeasureResolverFactory {

    private final IParser parser;
    private final ZipStreamProcessor zipProcessor;

    public R4QualityMeasureResolverFactory(IParser parser) {
        this(parser, new ZipStreamProcessor());
    }

    public R4QualityMeasureResolverFactory(IParser parser, ZipStreamProcessor zipProcessor) {
        this.parser = parser;
        this.zipProcessor = zipProcessor;
    }

    public R4QualityMeasureResolvers fromDirectory(Path directory, String... searchPaths) throws IOException {
        ResourceFieldHandler<Library, Identifier> libraryFieldHandler = new R4LibraryResourceFieldHandler();
        MapFhirResourceResolver<Library, Identifier> libraryResolver = createResolver(libraryFieldHandler);
        ResourceFieldHandler<Measure, Identifier> measureFieldHandler = new R4MeasureResourceFieldHandler();
        MapFhirResourceResolver<Measure, Identifier> measureResolver = createResolver(measureFieldHandler);

        try (Stream<Path> stream = Files.walk(directory, FileVisitOption.FOLLOW_LINKS)) {
            Iterator<Path> pathStream = stream
                    .filter(x -> HapiUtils.canParseFile(x.toString(), parser))
                    .filter(x -> PathHelper.isInSearchPaths(directory, x, searchPaths))
                    .iterator();
            while (pathStream.hasNext()) {
                Path path = pathStream.next();
                String content = readFile(path);
                consumeFile(path.toString(), content, libraryFieldHandler, libraryResolver, measureFieldHandler, measureResolver);
            }
        }

        return new R4QualityMeasureResolvers(libraryResolver, measureResolver);
    }

    public R4QualityMeasureResolvers fromZipFile(Path zipFile, String... searchPaths) throws IOException {
        try (InputStream is = new FileInputStream(zipFile.toFile())) {
            ZipInputStream zis = new ZipInputStream(is);
            return fromZipStream(zis, searchPaths);
        }
    }

    public R4QualityMeasureResolvers fromZipStream(ZipInputStream zis, String... searchPaths) throws IOException {
        ResourceFieldHandler<Library, Identifier> libraryFieldHandler = new R4LibraryResourceFieldHandler();
        MapFhirResourceResolver<Library, Identifier> libraryResolver = createResolver(libraryFieldHandler);
        ResourceFieldHandler<Measure, Identifier> measureFieldHandler = new R4MeasureResourceFieldHandler();
        MapFhirResourceResolver<Measure, Identifier> measureResolver = createResolver(measureFieldHandler);

        zipProcessor.processZip(zis, searchPaths, (filename, content) -> {
            consumeFile(filename, content, libraryFieldHandler, libraryResolver, measureFieldHandler, measureResolver);
        });

        return new R4QualityMeasureResolvers(libraryResolver, measureResolver);
    }

    private String readFile(Path path) {
        try {
            return IOUtils.toString(path.toUri(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void consumeFile(
            String filename,
            String content,
            ResourceFieldHandler<Library, Identifier> libraryFieldHandler,
            MapFhirResourceResolver<Library, Identifier> libraryResolver,
            ResourceFieldHandler<Measure, Identifier> measureFieldHandler,
            MapFhirResourceResolver<Measure, Identifier> measureResolver
    ) {
        String filenameId = FilenameUtils.getBaseName(filename);

        if (HapiUtils.canParseFile(filename, parser)) {
            IBaseResource baseResource = parser.parseResource(content);
            if (libraryFieldHandler.getSupportedClass().isInstance(baseResource)) {
                Library library = (Library)baseResource;
                setIdIfNotPresent(library, libraryFieldHandler, filenameId);
                libraryResolver.addResource(library);
            } else if (measureFieldHandler.getSupportedClass().isInstance(baseResource)) {
                Measure measure = (Measure)baseResource;
                setIdIfNotPresent(measure, measureFieldHandler, filenameId);
                measureResolver.addResource(measure);
            }
        }
    }

    private <T, I> void setIdIfNotPresent(T resource, ResourceFieldHandler<T, I> fieldHandler, String fallbackId) {
        String resourceId = fieldHandler.getId(resource);
        if (resourceId == null) {
            fieldHandler.setId(fallbackId, resource);
        }
    }

    private <T, I> MapFhirResourceResolver<T, I> createResolver(ResourceFieldHandler<T, I> fieldHandler) {
        ResourceSelector<T> resourceSelector = new ResourceSelector<>(fieldHandler);
        return new MapFhirResourceResolver<>(fieldHandler, resourceSelector);
    }
}
