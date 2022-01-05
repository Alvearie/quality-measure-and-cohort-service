/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PriorityCqlLibraryProvider implements CqlLibraryProvider {
    private final List<CqlLibraryProvider> providers = new ArrayList<>();

    public PriorityCqlLibraryProvider(CqlLibraryProvider primary, CqlLibraryProvider... others) {
        providers.add(primary);
        providers.addAll(Arrays.asList(others));
    }

    @Override
    public CqlLibrary getLibrary(CqlLibraryDescriptor cqlResourceDescriptor) {
        CqlLibrary library = null;
        for (CqlLibraryProvider provider : providers) {
            library = provider.getLibrary(cqlResourceDescriptor);
            if (library != null) {
                break;
            }
        }
        return library;
    }
}
