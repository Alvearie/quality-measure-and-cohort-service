/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import org.hl7.fhir.r4.model.Type;

public class UnsupportedFhirTypeException extends RuntimeException {

	private static final long serialVersionUID = -8667460433830276020L;
	
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
