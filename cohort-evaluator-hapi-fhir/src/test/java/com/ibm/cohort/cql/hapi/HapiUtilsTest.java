/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3._1999.xhtml.P;

public class HapiUtilsTest {

    @Test
    public void canParseFile_pass() {
        IParser parser = getParser();
        String filename = "file.json";
        boolean actual = HapiUtils.canParseFile(filename, parser);
        Assert.assertTrue(actual);
    }

    @Test
    public void canParseFile_passWithUppercase() {
        IParser parser = getParser();
        String filename = "file.JSON";
        boolean actual = HapiUtils.canParseFile(filename, parser);
        Assert.assertTrue(actual);
    }

    @Test
    public void canParseFile_fail() {
        IParser parser = getParser();
        String filename = "file.xml";
        boolean actual = HapiUtils.canParseFile(filename, parser);
        Assert.assertFalse(actual);
    }

    private IParser getParser() {
        IParser parser = Mockito.mock(IParser.class);
        Mockito.when(parser.getEncoding())
                .thenReturn(EncodingEnum.JSON);
        return parser;
    }

}
