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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.library.PriorityCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import com.ibm.cohort.cql.util.StringMatcher;

public abstract class BaseDataTypeRequirementsProcessorTest {

    protected CqlLibraryProvider createLibrarySourceProvider(String cqlPath, String modelInfoPath) throws IOException, FileNotFoundException {
        ClasspathCqlLibraryProvider cpBasedLp = new ClasspathCqlLibraryProvider("org.hl7.fhir");
        cpBasedLp.setSupportedFormats(Format.CQL);
        
        DirectoryBasedCqlLibraryProvider dirBasedLp = new DirectoryBasedCqlLibraryProvider(new File(cqlPath));
        PriorityCqlLibraryProvider lsp = new PriorityCqlLibraryProvider(dirBasedLp, cpBasedLp);
        
        CqlToElmTranslator translator = new CqlToElmTranslator();
        
        if( modelInfoPath != null ) {
            final File modelInfoFile = new File(modelInfoPath);
            try( Reader r = new FileReader( modelInfoFile ) ) {
                translator.registerModelInfo(r);
            }
        }
        CqlLibraryProvider sourceProvider = new TranslatingCqlLibraryProvider(lsp, translator);
        return sourceProvider;
    }

    protected Map<String, Set<String>> runPathTest(String cqlPath, String modelInfoPath, Set<String> expressions) throws IOException, FileNotFoundException {
        return runPathTest(cqlPath, modelInfoPath, expressions, null);
    }

    protected Map<String, Set<String>> runPathTest(String cqlPath, String modelInfoPath, Set<String> expressions, Predicate<CqlLibraryDescriptor> libraryFilter)
            throws IOException, FileNotFoundException {
                CqlLibraryProvider sourceProvider = createLibrarySourceProvider(cqlPath, modelInfoPath);
                
                DataTypeRequirementsProcessor requirementsProcessor = new DataTypeRequirementsProcessor();
                
                Map<String,Set<String>> pathsByDataType = new HashMap<>();
                for( CqlLibraryDescriptor cld : sourceProvider.listLibraries() ) {
                    if( libraryFilter == null || libraryFilter.test(cld) ) {
                        System.out.println("Processing " + cld.toString());
                        Map<String,Set<String>> newPaths = requirementsProcessor.getPathRequirementsByDataType(sourceProvider, cld, expressions);
                        
                        newPaths.forEach( (key,value) -> {
                            pathsByDataType.merge(key, value, (prev,current) -> { prev.addAll(current); return prev; } );
                        });
                    }
                }   
                
                //System.out.println(pathsByDataType);
                return pathsByDataType;
            }

    protected Map<String, Set<StringMatcher>> runPatternTest(String cqlPath, String modelInfoPath, Set<String> expressions) throws IOException, FileNotFoundException {
        return runPatternTest(cqlPath, modelInfoPath, expressions, null);
    }

    protected Map<String, Set<StringMatcher>> runPatternTest(String cqlPath, String modelInfoPath, Set<String> expressions, Predicate<CqlLibraryDescriptor> libraryFilter)
            throws IOException, FileNotFoundException {
                CqlLibraryProvider sourceProvider = createLibrarySourceProvider(cqlPath, modelInfoPath);
                
                DataTypeRequirementsProcessor requirementsProcessor = new DataTypeRequirementsProcessor();
                
                Map<String,Set<StringMatcher>> pathMatchersByDataType = new HashMap<>();
                for( CqlLibraryDescriptor cld : sourceProvider.listLibraries() ) {
                    if( libraryFilter == null || libraryFilter.test(cld) ) {
                        System.out.println("Processing " + cld.toString());
                        Map<String,Set<StringMatcher>> newPaths = requirementsProcessor.getAnyColumnRequirementsByDataType(sourceProvider, cld);
                        
                        newPaths.forEach( (key,value) -> {
                            pathMatchersByDataType.merge(key, value, (prev,current) -> { prev.addAll(current); return prev; } );
                        });
                    }
                }   
                
                System.out.println(pathMatchersByDataType);
                return pathMatchersByDataType;
            }

}
