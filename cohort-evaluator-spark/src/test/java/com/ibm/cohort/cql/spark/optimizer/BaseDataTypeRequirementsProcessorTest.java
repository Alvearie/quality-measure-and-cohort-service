/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryHelpers;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.Format;
import com.ibm.cohort.cql.library.MapCqlLibraryProviderFactory;
import com.ibm.cohort.cql.library.PriorityCqlLibraryProvider;
import com.ibm.cohort.cql.library.ZipStreamProcessor;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import com.ibm.cohort.cql.util.StringMatcher;

public abstract class BaseDataTypeRequirementsProcessorTest {

    protected CqlLibraryProvider createLibrarySourceProvider(String cqlPath, CqlToElmTranslator translator)
            throws IOException {
        ClasspathCqlLibraryProvider cpBasedLp = new ClasspathCqlLibraryProvider("org.hl7.fhir");
        cpBasedLp.setSupportedFormats(Format.CQL);

        MapCqlLibraryProviderFactory dirProviderFactory = new MapCqlLibraryProviderFactory(new ZipStreamProcessor());
        CqlLibraryProvider dirBasedLp = dirProviderFactory.fromDirectory(Paths.get(cqlPath));
        PriorityCqlLibraryProvider lsp = new PriorityCqlLibraryProvider(dirBasedLp, cpBasedLp);

        CqlLibraryProvider sourceProvider = new TranslatingCqlLibraryProvider(lsp, translator);
        return sourceProvider;
    }

    public CqlToElmTranslator createCqlTranslator(String modelInfoPath) throws IOException {
        CqlToElmTranslator translator = new CqlToElmTranslator();

        if (modelInfoPath != null) {
            final File modelInfoFile = new File(modelInfoPath);
            try (Reader r = new FileReader(modelInfoFile)) {
                translator.registerModelInfo(r);
            }
        }
        return translator;
    }

    protected Map<String, Set<String>> runPathTest(String cqlPath, String modelInfoPath, Set<String> expressions)
            throws IOException {
        return runPathTest(cqlPath, modelInfoPath, expressions, null);
    }

    protected Map<String, Set<String>> runPathTest(String cqlPath, String modelInfoPath, Set<String> expressions,
            Predicate<Path> libraryFilter) throws IOException {
        CqlToElmTranslator translator = createCqlTranslator(modelInfoPath);
        CqlLibraryProvider sourceProvider = createLibrarySourceProvider(cqlPath, translator);

        DataTypeRequirementsProcessor requirementsProcessor = new DataTypeRequirementsProcessor(translator);

        Map<String, Set<String>> pathsByDataType = new HashMap<>();
        if (libraryFilter == null) {
            libraryFilter = x -> true;
        }
        List<Path> paths = Files.list(Paths.get(cqlPath))
                .filter(libraryFilter)
                .collect(Collectors.toList());
        for (Path path : paths) {
            System.out.println("Processing " + path.toString());
            CqlLibraryDescriptor cld = CqlLibraryHelpers.filenameToLibraryDescriptor(path.toString());
            Map<String, Set<String>> newPaths = requirementsProcessor.getPathRequirementsByDataType(sourceProvider,
                    cld, expressions);

            newPaths.forEach((key, value) -> {
                pathsByDataType.merge(key, value, (prev, current) -> {
                    prev.addAll(current);
                    return prev;
                });
            });
        }

        // System.out.println(pathsByDataType);
        return pathsByDataType;
    }

    protected Map<String, Set<StringMatcher>> runPatternTest(String cqlPath, String modelInfoPath,
            Set<String> expressions) throws IOException {
        return runPatternTest(cqlPath, modelInfoPath, expressions, null);
    }

    protected Map<String, Set<StringMatcher>> runPatternTest(String cqlPath, String modelInfoPath,
            Set<String> expressions, Predicate<Path> libraryFilter)
            throws IOException {
        CqlToElmTranslator translator = createCqlTranslator(modelInfoPath);
        CqlLibraryProvider sourceProvider = createLibrarySourceProvider(cqlPath, translator);

        DataTypeRequirementsProcessor requirementsProcessor = new DataTypeRequirementsProcessor(translator);

        Map<String, Set<StringMatcher>> pathMatchersByDataType = new HashMap<>();
        if (libraryFilter == null) {
            libraryFilter = x -> true;
        }
        List<Path> paths = Files.list(Paths.get(cqlPath))
                .filter(libraryFilter)
                .collect(Collectors.toList());
        for (Path path : paths) {
            System.out.println("Processing " + path.toString());
            CqlLibraryDescriptor cld = CqlLibraryHelpers.filenameToLibraryDescriptor(path.toString());
            Map<String, Set<StringMatcher>> newPaths = requirementsProcessor
                    .getAnyColumnRequirementsByDataType(sourceProvider, cld);

            newPaths.forEach((key, value) -> {
                pathMatchersByDataType.merge(key, value, (prev, current) -> {
                    prev.addAll(current);
                    return prev;
                });
            });
        }

        //System.out.println(pathMatchersByDataType);
        return pathMatchersByDataType;
    }

}
