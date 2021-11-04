/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;


@RunWith(value=Parameterized.class)
public class PathDataTypeRequirementsTest extends BaseDataTypeRequirementsProcessorTest {
    
    private static File BASE_DIR = new File("src/test/resources");
    private static File CONFIG_DIR = new File(BASE_DIR, "alltypes");

    private ObjectMapper om = new ObjectMapper();
    
    @Parameter(value=0)
    public String name;
    
    @Parameter(value=1)
    public File testCaseDir;
    
    @Parameters(name = "{index}: checkPaths({0})")
    public static Collection<Object[]> data() {
        File testsDir = new File(BASE_DIR, "column-filtering");
        
        Collection<Object[]> data = new ArrayList<>();
        for( File testCaseDir : testsDir.listFiles( f -> f.isDirectory() ) ) {
            data.add( new Object[] { testCaseDir.getName(), testCaseDir } );
        }
        return data;
    }
    
    @Test
    public void checkPaths() throws Exception {
        Map<String,Set<String>> expectations = om.readValue(new File(testCaseDir,"expectations.json"), 
                om.getTypeFactory().constructParametricType(Map.class, String.class, Set.class));
        
        Set<String> expressions = null;
        File expressionsFile = new File(testCaseDir, "expressions.json");
        if( expressionsFile.exists() ) {
            expressions = om.readValue(expressionsFile, 
                    om.getTypeFactory().constructParametricType(Set.class, String.class));
        }
        
        Map<String,Set<String>> paths = runPathTest(testCaseDir.toString()
                , new File(CONFIG_DIR, "modelinfo/alltypes-modelinfo-1.0.0.xml").toString()
                , expressions
                , desc -> desc.getLibraryId().equals("Parent"));
        
        System.out.println(paths);
        
        assertEquals( expectations, paths );
    }
}
