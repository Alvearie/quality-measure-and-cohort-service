/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class FhirClientFactoryTest {
	@Test
	public void testSimpleFhirConfig() {
		FhirServerConfig simple = new FhirServerConfig();
		simple.setEndpoint("http://hapi.fhir.org/baseR4");

		FhirContext ctx = FhirContext.forR4();
		FhirClientFactory clientFactory = FhirClientFactory.newInstance(ctx);
		
		IGenericClient client = clientFactory.createFhirClient(simple);
		assertEquals(0, client.getInterceptorService().getAllRegisteredInterceptors().size());
	}
	
	@Test
	public void testIBMFhirConfig() {
		IBMFhirServerConfig config = new IBMFhirServerConfig();
		config.setEndpoint("https://localhost:9443/fhir-server/api/v4");
		config.setUser("fhiruser");
		config.setPassword("change-password");
		config.setTenantIdHeader(IBMFhirServerConfig.DEFAULT_TENANT_ID_HEADER);
		config.setTenantId("default");

		FhirContext ctx = FhirContext.forR4();
		FhirClientFactory clientFactory = FhirClientFactory.newInstance(ctx);
		
		IGenericClient client = clientFactory.createFhirClient(config);
		assertEquals(2, client.getInterceptorService().getAllRegisteredInterceptors().size());
	}
}
