/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.model;

import org.hl7.fhir.r4.model.Type;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;

// todo: [daniel.kim] not needed?
@DatatypeDef(name = "MeasureParameter")
public class MeasureParameter extends Type {
    private static final long serialVersionUID = 0L;

    @Override
    protected Type typedCopy() {
        return copy();
    }

    @Override
    public MeasureParameter copy() {
        MeasureParameter mp = new MeasureParameter();
        copyValues(mp);

        return mp;
    }

    public void copyValues(MeasureParameter mp) {
        super.copyValues(mp);
    }
}
