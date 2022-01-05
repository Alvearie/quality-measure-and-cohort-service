/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.terminology;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

/**
 * An implementation of {@link CqlTerminologyProvider} that queries the provided
 * {@link TerminologyProvider} instance.
 */
public class DefaultCqlTerminologyProvider implements CqlTerminologyProvider {

    private final TerminologyProvider terminologyProvider;

    public DefaultCqlTerminologyProvider(TerminologyProvider terminologyProvider) {
        this.terminologyProvider = terminologyProvider;
    }

    @Override
    public boolean in(Code code, ValueSetInfo valueSet) {
        return terminologyProvider.in(code, valueSet);
    }

    @Override
    public Iterable<Code> expand(ValueSetInfo valueSet) {
        return terminologyProvider.expand(valueSet);
    }

    @Override
    public Code lookup(Code code, CodeSystemInfo codeSystem) {
        return terminologyProvider.lookup(code, codeSystem);
    }

}
