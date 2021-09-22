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
 * are used for lookup per the CQL specification on filtered retrieves. Code equivalence
 * checking only considers the system and code values of the Code object. The version
 * and display properties are ignored.
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

    /**
     * Checks for equality between one CodeKey and another. Per the CQL Spec, the
     * "equivalence" operator is used when doing filtered retrieves. Equivalence
     * considers only the system and code properties of the <code>Code</code>
     * object. The display and version properties of the code are ignored.
     */
    @Override
    public boolean equals(Object o2) {
        Boolean codeEquals = super.equivalent(o2);
        return codeEquals != null && codeEquals;
    }

    /**
     * Computes object identity based on the same logic as Code equivalence. Only
     * the system and code properties of the <code>Code</code> object are used. The
     * display and version properties of the code are ignored.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (getCode() == null ? 0 : getCode().hashCode());
        hash = 31 * hash + (getSystem() == null ? 0 : getSystem().hashCode());
        return hash;
    }
}
