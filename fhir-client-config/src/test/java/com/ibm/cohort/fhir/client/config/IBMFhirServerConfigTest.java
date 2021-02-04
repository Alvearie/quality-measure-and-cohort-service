/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.fhir.client.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

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
		config.setDataSourceId("non-default");
		config.setDataSourceIdHeader("X-MY-DSID");
		
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
		assertEquals( config.getDataSourceId(), inflated.getDataSourceId());
		assertEquals( config.getDataSourceIdHeader(), inflated.getDataSourceIdHeader());
	}
	
	@Test
	public void testHeadersSetWhenNotNull() {
		IBMFhirServerConfig config = new IBMFhirServerConfig();
		config.setEndpoint("https://localhost:9443/fhir-server/api/v4");
		config.setTenantId("default");
		config.setTenantIdHeader("X-NOT-REAL");
		config.setDataSourceId("non-default");
		config.setDataSourceIdHeader("X-MY-DSID");
		
		Map<String,String> headers = config.getAdditionalHeaders();
		assertEquals(headers.get("X-NOT-REAL"), "default");
		assertEquals(headers.get("X-MY-DSID"), "non-default");
	}
	
	@Test
	public void testHeadersNotSetWhenNull() {
		IBMFhirServerConfig config = new IBMFhirServerConfig();
		config.setEndpoint("https://localhost:9443/fhir-server/api/v4");
		
		Map<String,String> headers = config.getAdditionalHeaders();
		assertFalse(headers.containsKey("X-NOT-REAL"));
		assertFalse(headers.containsKey("X-MY-DSID"));
	}
}
