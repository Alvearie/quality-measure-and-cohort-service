/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.terminology;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

public class UnsupportedTerminologyProvider implements CqlTerminologyProvider {

    @Override
    public boolean in(Code code, ValueSetInfo valueSet) {
        throw new UnsupportedOperationException("Terminology operations are not supported.");
    }

    @Override
    public Iterable<Code> expand(ValueSetInfo valueSet) {
        throw new UnsupportedOperationException("Terminology operations are not supported.");
    }

    @Override
    public Code lookup(Code code, CodeSystemInfo codeSystem) {
        throw new UnsupportedOperationException("Terminology operations are not supported.");
    }
}
