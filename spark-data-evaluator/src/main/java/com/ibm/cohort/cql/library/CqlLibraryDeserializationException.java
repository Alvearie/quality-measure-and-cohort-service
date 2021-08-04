/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

public class CqlLibraryDeserializationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CqlLibraryDeserializationException() {
        super();
    }

    public CqlLibraryDeserializationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CqlLibraryDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CqlLibraryDeserializationException(String message) {
        super(message);
    }

    public CqlLibraryDeserializationException(Throwable cause) {
        super(cause);
    }
}
