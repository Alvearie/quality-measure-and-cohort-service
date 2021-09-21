/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.model;

import org.opencds.cqf.cql.engine.runtime.Code;

/**
 * This class is used as the map key for a cache in the retrieve provider that indexes
 * data rows based on the codings contained in that data. Code equivalence checks
 * are used for lookup per the CQL specification on filtered retrieves.
 * 
 * @see <a href="https://cql.hl7.org/02-authorsguide.html#filtering-with-terminology">Filtering with Terminology</a>
 * @see <a href="https://cql.hl7.org/02-authorsguide.html#terminology-operators">Terminology Operations</a>
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
        Boolean codeEquals = super.equivalent(o2);
        return codeEquals != null && codeEquals;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (getCode() == null ? 0 : getCode().hashCode());
        hash = 31 * hash + (getSystem() == null ? 0 : getSystem().hashCode());
        return hash;
    }
}
