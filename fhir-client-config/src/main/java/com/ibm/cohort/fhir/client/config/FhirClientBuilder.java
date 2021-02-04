/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.fhir.client.config;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Defines the API to obtain IGenericClient instances from a FhirServerConfig
 * object. Implementers of this interface can customize the IGenericClient
 * instances however they need for their target environment.
 */
public interface FhirClientBuilder {
	/**
	 * Create an instance of a HAPI Fhir client configured with
	 * the provided configuration settings.
	 * 
	 * @param config Client configuration
	 * @return New HAPI Fhir client.
	 */
	public IGenericClient createFhirClient(FhirServerConfig config);
}
