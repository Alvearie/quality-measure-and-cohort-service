/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A utility to assist in processing files in a {@link ZipInputStream}.
 */
public class ZipStreamProcessor {

    public void processZip(ZipInputStream zis, BiConsumer<String, String> consumer) throws IOException {
        processZip(zis, new String[0], consumer);
    }

    public void processZip(ZipInputStream zis, String[] searchPaths, BiConsumer<String, String> consumer) throws IOException {
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            if (!ze.isDirectory()) {
                String fileName = ze.getName();
                boolean passesFilter = true;
                if (!ArrayUtils.isEmpty(searchPaths)) {
                    String prefix = "";
                    int ch;
                    if( (ch=fileName.lastIndexOf('/')) != -1 ) {
                        prefix = fileName.substring(0, ch);
                    }
                    passesFilter = ArrayUtils.contains(searchPaths, prefix);
                }

                if (passesFilter) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copy(zis, baos);
                    String content = baos.toString(StandardCharsets.UTF_8.name());
                    consumer.accept(fileName, content);
                }
            }
        }
    }

}
