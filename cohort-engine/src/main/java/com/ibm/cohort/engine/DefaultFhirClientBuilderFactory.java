/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import ca.uhn.fhir.context.FhirContext;

public class DefaultFhirClientBuilderFactory extends FhirClientBuilderFactory {
	public DefaultFhirClientBuilderFactory() {
		
	}
	
	@Override
	public DefaultFhirClientBuilder newFhirClientBuilder() {
		return new DefaultFhirClientBuilder(FhirContext.forR4());
	}
	
	@Override
	public DefaultFhirClientBuilder newFhirClientBuilder(FhirContext fhirContext) {
		return new DefaultFhirClientBuilder(fhirContext);
	}
}
