/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import ca.uhn.fhir.context.FhirContext;

/**
 * Platform default FhirClientBuilderFactory. This produces DefaultFhirClientBuilder
 * instances and uses R4 as the default FhirContext.
 */
public class DefaultFhirClientBuilderFactory extends FhirClientBuilderFactory {
	public DefaultFhirClientBuilderFactory() {
		
	}
	
	@Override
	public FhirClientBuilder newFhirClientBuilder() {
		return new DefaultFhirClientBuilder(FhirContext.forR4());
	}
	
	@Override
	public FhirClientBuilder newFhirClientBuilder(FhirContext fhirContext) {
		return new DefaultFhirClientBuilder(fhirContext);
	}
}
