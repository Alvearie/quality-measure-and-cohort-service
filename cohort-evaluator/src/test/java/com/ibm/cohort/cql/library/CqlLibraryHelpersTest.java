/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import org.junit.Assert;
import org.junit.Test;

public class CqlLibraryHelpersTest {

    @Test
    public void filenameToLibraryDescriptor() {
        String id = "id";
        String version = "1.0.0";
        String suffix = "xml";
        String filename = id + "-" + version + "." + suffix;
        CqlLibraryDescriptor actual = CqlLibraryHelpers.filenameToLibraryDescriptor(filename);
        CqlLibraryDescriptor expected = new CqlLibraryDescriptor()
                .setLibraryId(id)
                .setVersion(version)
                .setFormat(Format.ELM);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void filenameToLibraryDescriptor_noVersion() {
        String id = "id";
        String suffix = "xml";
        String filename = id + "." + suffix;
        CqlLibraryDescriptor actual = CqlLibraryHelpers.filenameToLibraryDescriptor(filename);
        CqlLibraryDescriptor expected = new CqlLibraryDescriptor()
                .setLibraryId(id)
                .setFormat(Format.ELM);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void filenameToLibraryDescriptor_invalidSuffix() {
        String id = "id";
        String version = "1.0.0";
        String suffix = "cobol";
        String filename = id + "-" + version + "." + suffix;
        CqlLibraryDescriptor actual = CqlLibraryHelpers.filenameToLibraryDescriptor(filename);
        Assert.assertNull(actual);
    }

    @Test
    public void filenameToLibraryDescriptor_basenameEndsWithHyphen() {
        String id = "id-1.0.0-";
        String suffix = "xml";
        String filename = id + "." + suffix;

        CqlLibraryDescriptor actual = CqlLibraryHelpers.filenameToLibraryDescriptor(filename);
        CqlLibraryDescriptor expected = new CqlLibraryDescriptor()
                .setLibraryId(id)
                .setFormat(Format.ELM);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void isFileReadable_pass() {
        Assert.assertTrue(CqlLibraryHelpers.isFileReadable("file.xml"));
    }

    @Test
    public void isFileReadable_fail() {
        Assert.assertFalse(CqlLibraryHelpers.isFileReadable("file.cobol"));
    }

    @Test
    public void libraryDescriptorToFilename_xml() {
        String id = "id";
        String version = "1.0.0";
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId(id)
                .setVersion(version)
                .setFormat(Format.ELM);
        String actual = CqlLibraryHelpers.libraryDescriptorToFilename(descriptor);
        String expected = id + "-" + version + ".xml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void libraryDescriptorToFilename_cql() {
        String id = "id";
        String version = "1.0.0";
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId(id)
                .setVersion(version)
                .setFormat(Format.CQL);
        String actual = CqlLibraryHelpers.libraryDescriptorToFilename(descriptor);
        String expected = id + "-" + version + ".cql";
        Assert.assertEquals(expected, actual);
    }

}
