/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class FormatTest {

    @Test
    public void testCqlAlias() {
        Assert.assertEquals(Format.CQL, Format.lookupByName("cql"));
    }

    @Test
    public void testTextCqlAlias() {
        Assert.assertEquals(Format.CQL, Format.lookupByName("text/cql"));
    }

    @Test
    public void testApplicationCqlAlias() {
        Assert.assertEquals(Format.CQL, Format.lookupByName("application/cql"));
    }

    @Test
    public void testXmlAlias() {
        Assert.assertEquals(Format.ELM, Format.lookupByName("xml"));
    }

    @Test
    public void testCapitalXmlAlias() {
        Assert.assertEquals(Format.ELM, Format.lookupByName("XML"));
    }

    @Test
    public void testApplicationElmXmlAlias() {
        Assert.assertEquals(Format.ELM, Format.lookupByName("application/elm+xml"));
    }

    @Test
    public void testUnknownAlias() {
        Assert.assertNull(Format.lookupByName("unknown"));
    }

    @Test
    public void testGetNames() {
        Assert.assertEquals(
                Arrays.asList("CQL", "cql", "text/cql", "application/cql"),
                Format.CQL.getNames()
        );
    }

}
