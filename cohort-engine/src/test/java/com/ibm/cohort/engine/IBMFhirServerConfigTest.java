/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class IBMFhirServerConfigTest {
	@Test
	public void testSerializeDeserialize() throws Exception {
		IBMFhirServerConfig config = new IBMFhirServerConfig();
		config.setEndpoint("https://localhost:9443/fhir-server/api/v4");
		config.setUser("fhiruser");
		config.setPassword("change-password");
		config.setTenantId("default");
		config.setTenantIdHeader("X-NOT-REAL");
		
		ObjectMapper om = new ObjectMapper();
		String deflated = om.writeValueAsString( config );
		System.out.println(deflated);
		
		IBMFhirServerConfig inflated = (IBMFhirServerConfig) om.readValue( deflated, FhirServerConfig.class );
		assertNotNull( inflated );
		assertEquals( config.getEndpoint(), inflated.getEndpoint());
		assertEquals( config.getUser(), inflated.getUser());
		assertEquals( config.getPassword(), inflated.getPassword());
		assertEquals( config.getTenantId(), inflated.getTenantId());
		assertEquals( config.getTenantIdHeader(), inflated.getTenantIdHeader());
	}
}
