/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

public class FactoryConfigurationError extends Error {
	private static final long serialVersionUID = 43378842898962170L;
	
	public FactoryConfigurationError(String message) {
		super(message);
	}
	
	public FactoryConfigurationError(Exception ex) {
		super(ex);
	}
	
	public FactoryConfigurationError(String message, Exception ex) {
		super(message, ex);
	}
}
