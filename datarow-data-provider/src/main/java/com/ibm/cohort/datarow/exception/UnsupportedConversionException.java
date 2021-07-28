/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.exception;

/**
 * Indicates that data type conversion that would be necessary to support
 * a given runtime scenario is not supported by the application.
 */
public class UnsupportedConversionException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public UnsupportedConversionException() {
		super();
	}

	public UnsupportedConversionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnsupportedConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedConversionException(String message) {
		super(message);
	}

	public UnsupportedConversionException(Throwable cause) {
		super(cause);
	}
}
