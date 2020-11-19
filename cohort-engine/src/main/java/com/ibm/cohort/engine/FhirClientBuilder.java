/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public interface FhirClientBuilder {
	public IGenericClient createFhirClient(FhirServerConfig config);
}
