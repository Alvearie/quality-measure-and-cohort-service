/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.translator.provider;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.junit.Before;
import org.junit.Test;

public class ModelInfoReaderTest {

    private File file;
    
    @Before
    public void setUp() {
        file = new File("src/test/resources/modelinfo/ig-with-target-modelinfo-0.0.1.xml");
    }

    @Test
    public void testReadInputStream() throws Exception {
        try( InputStream is = new FileInputStream(file) ) {
            ModelInfo modelInfo = ModelInfoReader.convertToModelInfo(is);
            assertNotNull(modelInfo);
        }
    }
    
    @Test
    public void testReadReader() throws Exception {
        try( Reader r = new FileReader(file) ) {
            ModelInfo modelInfo = ModelInfoReader.convertToModelInfo(r);
            assertNotNull(modelInfo);
        }
    }
    
    @Test
    public void testReadFile() {
        ModelInfo modelInfo = ModelInfoReader.convertToModelInfo(file);
        assertNotNull(modelInfo);
    }
}
