/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FhirServerConfigTest {
	@Test
	public void testSerializeDeserialize() throws Exception {
		FhirServerConfig config = new FhirServerConfig();
		config.setEndpoint("https://localhost:9443/fhir-server/api/v4");
		config.setUser("fhiruser");
		config.setPassword("change-password");
		
		ObjectMapper om = new ObjectMapper();
		String deflated = om.writeValueAsString( config );
		
		FhirServerConfig inflated = om.readValue( deflated, FhirServerConfig.class );
		assertNotNull( inflated );
		assertEquals( config.getEndpoint(), inflated.getEndpoint());
		assertEquals( config.getUser(), inflated.getUser());
		assertEquals( config.getPassword(), inflated.getPassword());
	}
	
	@Test
	public void testSerializeExcludesNullValues() throws Exception {
		FhirServerConfig config = new FhirServerConfig();
		config.setEndpoint("http://hapi.fhir.org/baseR4");
		
		ObjectMapper om = new ObjectMapper();
		String deflated = om.writeValueAsString( config );
		
		System.out.println(deflated);
		assertFalse( deflated.contains("null") );
	}
}
