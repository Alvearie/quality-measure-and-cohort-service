/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MapCqlLibraryProviderTest {

    @Test
    public void getLibrary() {
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("id")
                .setVersion("1.0.0")
                .setFormat(Format.ELM);
        CqlLibrary expected = new CqlLibrary()
                .setDescriptor(descriptor)
                .setContent("content");

        Map<CqlLibraryDescriptor, CqlLibrary> map = new HashMap<>();
        map.put(descriptor, expected);

        MapCqlLibraryProvider provider = new MapCqlLibraryProvider(map);

        CqlLibrary actual = provider.getLibrary(descriptor);
        Assert.assertEquals(expected, actual);
    }

}
