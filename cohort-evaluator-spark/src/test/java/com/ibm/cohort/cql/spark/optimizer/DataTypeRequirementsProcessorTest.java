/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ibm.cohort.cql.util.PrefixStringMatcher;
import com.ibm.cohort.cql.util.RegexStringMatcher;
import com.ibm.cohort.cql.util.StringMatcher;


public class DataTypeRequirementsProcessorTest extends BaseDataTypeRequirementsProcessorTest {

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
        
        Map<String,Set<String>> pathsByDataType = runPathTest(cqlPath, modelPath, expressions, x -> x.getFileName().toString().equals("MeasureAB-1.0.0.cql"));
        
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
        
        Map<String,Set<String>> pathsByDataType = runPathTest(cqlPath, modelPath, expressions, x -> x.getFileName().toString().equals("Parent-1.0.0.cql"));
        
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
        Map<String,Set<String>> pathsByDataType;
        pathsByDataType = runPathTest("src/test/resources/fhir/cql", null, null);

        Map<String,Set<String>> expectations = new HashMap<>();
        expectations.put("Patient", new HashSet<>(Arrays.asList("name", "birthDate")));
        expectations.put("Condition", new HashSet<>(Arrays.asList("code", "id", "recordedDate")));
        expectations.put("Encounter", new HashSet<>(Arrays.asList("diagnosis", "period")));
        expectations.put("Encounter.Diagnosis", new HashSet<>(Arrays.asList("condition")));
        expectations.put("Observation", new HashSet<>(Arrays.asList("effective")));
        expectations.put("Period", new HashSet<>(Arrays.asList("start", "end")));
        expectations.put("Reference", new HashSet<>(Arrays.asList("reference")));
        expectations.put("dateTime", new HashSet<>(Arrays.asList("value")));
        
        assertEquals( expectations, pathsByDataType );
    }
    
    @Test
    public void testAnyColumnRequirements() throws Exception {
        String basePath = "src/test/resources";
        
        Map<String, Set<StringMatcher>> reqsByDataType = runPatternTest(basePath + "/any-column/cql",
                basePath + "/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml", null, x -> x.getFileName().toString().equals("MeasureAnyColumn-1.0.0.cql"));
        
        Map<String,Set<StringMatcher>> expectations = new HashMap<>();
        expectations.put("A", new HashSet<>(Arrays.asList(new PrefixStringMatcher("code_col"))));
        expectations.put("B", new HashSet<>(Arrays.asList(new RegexStringMatcher("integer_col"))));
        expectations.put("C", new HashSet<>(Arrays.asList(new RegexStringMatcher(".*_decimal"))));
        
        assertEquals( expectations, reqsByDataType );
    }
    
    @Test
    public void testMeasureAnyColumnPathRequirements() throws Exception {
        String basePath = "src/test/resources";
        
        Map<String, Set<String>> reqsByDataType = runPathTest(basePath + "/any-column/cql",
                basePath + "/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml", null, x -> x.getFileName().toString().equals("MeasureAnyColumn-1.0.0.cql"));
        
        Map<String,Set<String>> expectations = new HashMap<>();
        expectations.put("A", new HashSet<>(Arrays.asList("decimal_col")));
        expectations.put("B", new HashSet<>());
        expectations.put("C", new HashSet<>(Arrays.asList("big_decimal")));
        
        assertEquals( expectations, reqsByDataType );
    }
    
    @Test
    public void testMeasureAnyColumnChildPathRequirements() throws Exception {
        String basePath = "src/test/resources";
        
        Map<String, Set<String>> reqsByDataType = runPathTest(basePath + "/any-column/cql",
                basePath + "/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml", null, x -> x.getFileName().toString().equals("AnyColumnChild-1.0.0.cql"));
        
        Map<String,Set<String>> expectations = new HashMap<>();
        expectations.put("A", new HashSet<>(Arrays.asList("pat_id", "decimal_col", "string_col")));
        expectations.put("B", new HashSet<>());
        expectations.put("C", new HashSet<>(Arrays.asList("big_decimal", "double")));
        
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

}
