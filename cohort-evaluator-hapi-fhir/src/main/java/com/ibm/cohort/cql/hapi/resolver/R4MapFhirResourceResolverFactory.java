/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.parser.IParser;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.version.ResourceSelector;
import com.ibm.cohort.cql.fhir.handler.ResourceHandler;
import com.ibm.cohort.cql.fhir.resolver.MapFhirResourceResolver;
import com.ibm.cohort.cql.hapi.HapiUtils;
import com.ibm.cohort.cql.helpers.PathHelper;
import com.ibm.cohort.cql.library.ZipStreamProcessor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.ZipInputStream;

/**
 * A utility for assisting in creating {@link MapFhirResourceResolver} instances
 * from arbitrary packaged form factors (e.g. directories, zip files, etc).
 *
 * @param <T> The type to resolve.
 */
public class R4MapFhirResourceResolverFactory<T> {

    private final ResourceHandler<T, Identifier> resourceHandler;
    private final ResourceSelector<T> resourceSelector;
    private final IParser parser;
    private final ZipStreamProcessor zipProcessor;

    public R4MapFhirResourceResolverFactory(
            ResourceHandler<T, Identifier> resourceHandler,
            ResourceSelector<T> resourceSelector,
            IParser parser,
            ZipStreamProcessor zipProcessor
    ) {
        this.resourceHandler = resourceHandler;
        this.resourceSelector = resourceSelector;
        this.parser = parser;
        this.zipProcessor = zipProcessor;
    }

    public FhirResourceResolver<T> fromDirectory(Path directory, String... searchPaths) throws IOException {
        MapFhirResourceResolver<T, Identifier> retVal = new MapFhirResourceResolver<>(resourceHandler, resourceSelector);

        Class<T> handlerSupportedClass = resourceHandler.getSupportedClass();
        Iterator<Path> pathStream = Files.walk(directory, FileVisitOption.FOLLOW_LINKS)
                .filter(x -> HapiUtils.canParseFile(x.toString(), parser))
                .filter(x -> PathHelper.isInSearchPaths(directory, x, searchPaths))
                .iterator();
        while(pathStream.hasNext()) {
            Path path = pathStream.next();
            String content = readFile(path);
            IBaseResource baseResource = parser.parseResource(content);
            if (handlerSupportedClass.isInstance(baseResource)) {
                T resource = handlerSupportedClass.cast(baseResource);
                String fallbackId = FilenameUtils.getBaseName(path.getFileName().toString());
                setIdIfNotPresent(resource, fallbackId);
                retVal.addResource(resource);
            }
        }

        return retVal;
    }

    public FhirResourceResolver<T> fromZipFile(Path zipFile, String... searchPaths) throws IOException {
        try (InputStream is = new FileInputStream(zipFile.toFile())) {
            ZipInputStream zis = new ZipInputStream(is);
            return fromZipStream(zis, searchPaths);
        }
    }

    public FhirResourceResolver<T> fromZipStream(ZipInputStream zis, String... searchPaths) throws IOException {
        MapFhirResourceResolver<T, Identifier> retVal = new MapFhirResourceResolver<>(resourceHandler, resourceSelector);

        zipProcessor.processZip(zis, searchPaths, (filename, content) -> {
            if (HapiUtils.canParseFile(filename, parser)) {
                IBaseResource baseResource = parser.parseResource(content);
                Class<T> handlerSupportedClass = resourceHandler.getSupportedClass();
                if (handlerSupportedClass.isInstance(baseResource)) {
                    T resource = handlerSupportedClass.cast(baseResource);
                    String filenameId = FilenameUtils.getBaseName(filename);
                    setIdIfNotPresent(resource, filenameId);
                    retVal.addResource(resource);
                }
            }
        });

        return retVal;
    }

    private void setIdIfNotPresent(T resource, String fallbackId) {
        String resourceId = resourceHandler.getId(resource);
        if (resourceId == null) {
            resourceHandler.setId(fallbackId, resource);
        }
    }

    private String readFile(Path path) {
        try {
            return IOUtils.toString(path.toUri(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
