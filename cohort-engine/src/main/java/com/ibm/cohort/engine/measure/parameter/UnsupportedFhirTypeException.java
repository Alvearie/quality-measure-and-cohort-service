/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.parameter;

import org.hl7.fhir.r4.model.Type;

public class UnsupportedFhirTypeException extends RuntimeException {

    private final Type type;

    public UnsupportedFhirTypeException(Type type) {
        this.type = type;
    }

    @Override
    public String getMessage() {
        String className = (type == null) ?
                "null" :
                type.getClass().getSimpleName();

        return String.format("Unsupported FHIR type: `%s`", className);
    }
}
