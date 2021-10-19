/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Test;

import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.library.PriorityCqlLibraryProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.util.StringMatcher;
import com.ibm.cohort.cql.util.PrefixStringMatcher;

public class DataTypeRequirementsProcessorTest {

    @Test
    public void testCQLWithAllTypesModel() throws Exception {
        String basePath = "src/test/resources/alltypes";
        String cqlPath = basePath + "/cql";
        String modelPath = basePath + "/modelinfo/alltypes-modelinfo-1.0.0.xml";
        Set<String> expressions = Collections.singleton("cohort");
        
        Map<String,Set<String>> pathsByDataType = runPathTest(cqlPath, modelPath, expressions);
        
        Map<String,Set<String>> expectations = new HashMap<>();
        expectations.put("A", new HashSet<>(Arrays.asList("string_col", "code_col", "datetime_col", "integer_col", "boolean_col", "code_col2", "decimal_col", "date_col")));
        expectations.put("B", new HashSet<>(Arrays.asList("date", "datetime", "boolean", "string", "integer", "decimal")));
        expectations.put("C", new HashSet<>(Arrays.asList("big_decimal", "double", "float")));
        expectations.put("D", new HashSet<>(Arrays.asList("short", "integer", "long")));

        
        assertEquals( expectations, pathsByDataType );
    }
    
    @Test
    public void testCQLWithAllTypesModelMeasureABOnly() throws Exception {
        String basePath = "src/test/resources/alltypes";
        String cqlPath = basePath + "/cql";
        String modelPath = basePath + "/modelinfo/alltypes-modelinfo-1.0.0.xml";
        Set<String> expressions = Collections.singleton("cohort");
        
        Map<String,Set<String>> pathsByDataType = runPathTest(cqlPath, modelPath, expressions, (cld) -> cld.getLibraryId().equals("MeasureAB"));
        
        Map<String,Set<String>> expectations = new HashMap<>();
        expectations.put("A", new HashSet<>(Arrays.asList("code_col", "boolean_col")));
        expectations.put("B", new HashSet<>(Arrays.asList("date", "decimal")));
        
        assertEquals( expectations, pathsByDataType );
    }
    
    @Test
    public void testCQLWithAllTypesModelParentChildGrandChild() throws Exception {
        String basePath = "src/test/resources/alltypes";
        String cqlPath = basePath + "/cql";
        String modelPath = basePath + "/modelinfo/alltypes-modelinfo-1.0.0.xml";
        Set<String> expressions = Collections.singleton("cohort");
        
        Map<String,Set<String>> pathsByDataType = runPathTest(cqlPath, modelPath, expressions, (cld) -> cld.getLibraryId().equals("Parent"));
        
        Map<String,Set<String>> expectations = new HashMap<>();
        expectations.put("A", new HashSet<>(Arrays.asList("code_col", "boolean_col")));
        
        assertEquals( expectations, pathsByDataType );
    }
    
    @Test
    public void testCQLWithOutputValidationModel() throws Exception {
        String basePath = "src/test/resources/output-validation";
        String cqlPath = basePath + "/cql";
        String modelPath = basePath + "/modelinfo/simple-all-types-model-info.xml";
        Set<String> expressions = null;
        
        Map<String,Set<String>> pathsByDataType = runPathTest(cqlPath, modelPath, expressions);
        
        Map<String,Set<String>> expectations = new HashMap<>();
        expectations.put("Type1", new HashSet<>(Arrays.asList("boolean", "integer", "decimal", "string")));
        expectations.put("Type2", new HashSet<>(Arrays.asList("boolean", "date", "datetime")));
        expectations.put("Patient", new HashSet<>(Arrays.asList()));
        
        
        assertEquals( expectations, pathsByDataType );
    }
    
    @Test
    public void testCQLWithFHIRModel() throws Exception {
        Map<String,Set<String>> pathsByDataType = runPathTest("src/test/resources/fhir/cql", null, null);
        
        Map<String,Set<String>> expectations = new HashMap<>();
        expectations.put("Patient", new HashSet<>(Arrays.asList("birthDate")));
        expectations.put("Condition", new HashSet<>(Arrays.asList("code", "subject", "recordedDate")));
        expectations.put("Encounter", new HashSet<>(Arrays.asList("period", "period.end")));
        expectations.put("Observation", new HashSet<>(Arrays.asList("code")));
        
        assertEquals( expectations, pathsByDataType );
    }
    
    @Test
    public void testCQLWithAnyColumn() throws Exception {
        String basePath = "src/test/resources";
        
        Map<String, Set<StringMatcher>> reqsByDataType = runPatternTest(basePath + "/any-column/cql",
                basePath + "/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml", null);
        
        Map<String,Set<StringMatcher>> expectations = new HashMap<>();
        expectations.put("A", new HashSet<>(Arrays.asList(new PrefixStringMatcher("code_col"))));
        
        assertEquals( expectations, reqsByDataType );
    }

// This test case only matters if the user is allowed to pass non-null expression
// lists and that is currently blocked.
//    @Test
//    public void testMissingExpression() throws Exception {
//        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
//                () -> runTest("src/test/resources/fhir/cql", null, Collections.singleton("DoesNotExist")));
//        
//        assertEquals( "One or more requested expressions [DoesNotExist] not found in library {\"libraryId\":\"FhirTest\",\"version\":\"1.0.0\",\"format\":\"ELM\"}", ex.getMessage() );
//    }
    
    protected Map<String, Set<String>> runPathTest(String cqlPath, String modelInfoPath, Set<String> expressions) throws IOException, FileNotFoundException {
        return runPathTest(cqlPath, modelInfoPath, expressions, null);
    }

    protected Map<String, Set<String>> runPathTest(String cqlPath, String modelInfoPath, Set<String> expressions, Predicate<CqlLibraryDescriptor> libraryFilter) throws IOException, FileNotFoundException {
        CqlLibraryProvider cpBasedLp = new ClasspathCqlLibraryProvider("org.hl7.fhir");
        DirectoryBasedCqlLibraryProvider dirBasedLp = new DirectoryBasedCqlLibraryProvider(new File(cqlPath));
        PriorityCqlLibraryProvider lsp = new PriorityCqlLibraryProvider(dirBasedLp, cpBasedLp);
        
        CqlToElmTranslator translator = new CqlToElmTranslator();
        
        if( modelInfoPath != null ) {
            final File modelInfoFile = new File(modelInfoPath);
            try( Reader r = new FileReader( modelInfoFile ) ) {
                translator.registerModelInfo(r);
            }
        }
        
        DataTypeRequirementsProcessor requirementsProcessor = new DataTypeRequirementsProcessor(translator);
        
        Map<String,Set<String>> pathsByDataType = new HashMap<>();
        for( CqlLibraryDescriptor cld : lsp.listLibraries() ) {
            if( libraryFilter == null || libraryFilter.test(cld) ) {
                System.out.println("Processing " + cld.toString());
                Map<String,Set<String>> newPaths = requirementsProcessor.getPathsByDataType(lsp, cld);
                
                newPaths.forEach( (key,value) -> {
                    pathsByDataType.merge(key, value, (prev,current) -> { prev.addAll(current); return prev; } );
                });
            } else {
                System.out.println("Skipping " + cld.toString());
            }
        }   
        
        System.out.println(pathsByDataType);
        return pathsByDataType;
    }
    
    protected Map<String, Set<StringMatcher>> runPatternTest(String cqlPath, String modelInfoPath, Set<String> expressions) throws IOException, FileNotFoundException {
        return runPatternTest(cqlPath, modelInfoPath, expressions, null);
    }

    protected Map<String, Set<StringMatcher>> runPatternTest(String cqlPath, String modelInfoPath, Set<String> expressions, Predicate<CqlLibraryDescriptor> libraryFilter) throws IOException, FileNotFoundException {
        CqlLibraryProvider cpBasedLp = new ClasspathCqlLibraryProvider("org.hl7.fhir");
        DirectoryBasedCqlLibraryProvider dirBasedLp = new DirectoryBasedCqlLibraryProvider(new File(cqlPath));
        PriorityCqlLibraryProvider lsp = new PriorityCqlLibraryProvider(dirBasedLp, cpBasedLp);
        
        CqlToElmTranslator translator = new CqlToElmTranslator();
        
        if( modelInfoPath != null ) {
            final File modelInfoFile = new File(modelInfoPath);
            try( Reader r = new FileReader( modelInfoFile ) ) {
                translator.registerModelInfo(r);
            }
        }
        
        DataTypeRequirementsProcessor requirementsProcessor = new DataTypeRequirementsProcessor(translator);
        
        Map<String,Set<StringMatcher>> pathsByDataType = new HashMap<>();
        for( CqlLibraryDescriptor cld : lsp.listLibraries() ) {
            if( libraryFilter == null || libraryFilter.test(cld) ) {
                System.out.println("Processing " + cld.toString());
                Map<String,Set<StringMatcher>> newPaths = requirementsProcessor.getRequirementsByDataType(lsp, cld);
                
                newPaths.forEach( (key,value) -> {
                    pathsByDataType.merge(key, value, (prev,current) -> { prev.addAll(current); return prev; } );
                });
            } else {
                System.out.println("Skipping " + cld.toString());
            }
        }   
        
        System.out.println(pathsByDataType);
        return pathsByDataType;
    }
}
