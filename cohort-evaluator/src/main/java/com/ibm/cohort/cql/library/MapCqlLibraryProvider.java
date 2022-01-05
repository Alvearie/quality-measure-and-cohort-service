/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.util.Map;

/**
 * A simple implementation of {@link CqlLibraryProvider} that queries the
 * provided {@link Map}.
 */
public class MapCqlLibraryProvider implements CqlLibraryProvider {

    private final Map<CqlLibraryDescriptor, CqlLibrary> map;

    public MapCqlLibraryProvider(Map<CqlLibraryDescriptor, CqlLibrary> map) {
        this.map = map;
    }

    @Override
    public CqlLibrary getLibrary(CqlLibraryDescriptor cqlResourceDescriptor) {
        return map.get(cqlResourceDescriptor);
    }

}
