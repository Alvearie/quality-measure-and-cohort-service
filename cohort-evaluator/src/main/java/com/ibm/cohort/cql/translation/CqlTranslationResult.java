/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import java.util.Collection;

import com.ibm.cohort.cql.library.CqlLibrary;

public class CqlTranslationResult {

    private final CqlLibrary mainLibrary;
    private final Collection<CqlLibrary> dependencies;

    public CqlTranslationResult(CqlLibrary mainLibrary, Collection<CqlLibrary> dependencies) {
        this.mainLibrary = mainLibrary;
        this.dependencies = dependencies;
    }

    public CqlLibrary getMainLibrary() {
        return mainLibrary;
    }

    public Collection<CqlLibrary> getDependencies() {
        return dependencies;
    }
}
