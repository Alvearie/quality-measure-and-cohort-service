/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipInputStream;

public class ZipStreamProcessorTest {

    private static final String ZIP_RESOURCE = "/cql/multiple-files/packaged-cqls.zip";

    private final ZipStreamProcessor processor = new ZipStreamProcessor();

    @Test
    public void processZip() throws IOException {
        List<String> actualFilenames = new ArrayList<>();
        List<String> actualContents = new ArrayList<>();
        try(ZipInputStream zis = getStream()) {
            processor.processZip(zis, (filename, content) -> {
                actualFilenames.add(filename);
                actualContents.add(content);
            });
        }

        List<String> expectedFilenames = Arrays.asList(
                "BreastCancerScreening-1.0.0.cql",
                "FHIRHelpers-4.0.0.cql",
                "Test-1.0.0.cql",
                "sub-folder/TestDateQuery-1.0.0.cql"
        );
        Assert.assertEquals(expectedFilenames, actualFilenames);

        List<String> expectedSnippets = Arrays.asList(
                "library \"BreastCancerScreening\" version '1.0.0'",
                "library FHIRHelpers version '4.0.0'",
                "library \"Test\" version '1.0.0'",
                "library \"TestDateQuery\" version '1.0.0'"
        );
        performSnippetAssertion(expectedSnippets, actualContents);
    }

    @Test
    public void processZip_withSearchPaths() throws IOException {
        List<String> actualFilenames = new ArrayList<>();
        List<String> actualContents = new ArrayList<>();
        String[] searchPaths = new String[]{ "sub-folder" };
        try(ZipInputStream zis = getStream()) {
            processor.processZip(zis, searchPaths, (filename, content) -> {
                actualFilenames.add(filename);
                actualContents.add(content);
            });
        }

        List<String> expectedFilenames = Arrays.asList(
                "sub-folder/TestDateQuery-1.0.0.cql"
        );
        Assert.assertEquals(expectedFilenames, actualFilenames);

        List<String> expectedSnippets = Arrays.asList(
                "library \"TestDateQuery\" version '1.0.0'"
        );
        performSnippetAssertion(expectedSnippets, actualContents);
    }

    private void performSnippetAssertion(List<String> expectedSnippets, List<String> actualContents) {
        Assert.assertEquals(expectedSnippets.size(), actualContents.size());
        for (int i = 0; i < actualContents.size(); i++) {
            String expectedSnippet = expectedSnippets.get(i);
            String actual = actualContents.get(i);
            Assert.assertTrue(actual.contains(expectedSnippet));
        }
    }

    private ZipInputStream getStream() {
        InputStream is = this.getClass().getResourceAsStream(ZIP_RESOURCE);
        return new ZipInputStream(is);
    }
}
