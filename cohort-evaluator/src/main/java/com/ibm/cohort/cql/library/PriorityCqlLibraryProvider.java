package com.ibm.cohort.cql.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PriorityCqlLibraryProvider implements CqlLibraryProvider {
    private List<CqlLibraryProvider> providers = new ArrayList<>();

    public PriorityCqlLibraryProvider(CqlLibraryProvider primary, CqlLibraryProvider... others) {
        providers.add(primary);
        providers.addAll(Arrays.asList(others));
    }

    @Override
    public Collection<CqlLibraryDescriptor> listLibraries() {
        Set<CqlLibraryDescriptor> libraries = new HashSet<>();
        for (CqlLibraryProvider provider : providers) {
            libraries.addAll(provider.listLibraries());
        }
        return libraries;
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
