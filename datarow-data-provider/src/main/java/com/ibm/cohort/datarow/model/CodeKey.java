/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.model;

import org.opencds.cqf.cql.engine.runtime.Code;

/**
 * The CQL Code type doesn't implement equals, so those objects don't make a
 * good index key. We provide an adapter here that implements the standard
 * equals and hashCode methods so that we can use codes as Map keys.
 */
public class CodeKey extends Code {

    public CodeKey() {

    }

    public CodeKey(Code otherCode) {
        this.withCode(otherCode.getCode()).withSystem(otherCode.getSystem()).withDisplay(otherCode.getDisplay())
                .withVersion(otherCode.getVersion());
    }

    @Override
    public boolean equals(Object o2) {
        return super.equal(o2);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (getCode() == null ? 0 : getCode().hashCode());
        hash = 31 * hash + (getSystem() == null ? 0 : getSystem().hashCode());
        hash = 31 * hash + (getDisplay() == null ? 0 : getDisplay().hashCode());
        return hash;
    }
}
