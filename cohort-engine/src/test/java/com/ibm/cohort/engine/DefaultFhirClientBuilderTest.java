/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.FhirServerConfig.LogInfo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class DefaultFhirClientBuilderTest {
	
	static FhirContext ctx;
	DefaultFhirClientBuilder builder;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		// This step is expensive, so we cache if for all the test fixtures
		ctx = FhirContext.forR4();
	}
	
	@Before
	public void setUp() {
		 builder = new DefaultFhirClientBuilder(ctx);
	}
	
	@Test
	public void testSimpleFhirConfig() {
		FhirServerConfig simple = new FhirServerConfig();
		simple.setEndpoint("http://hapi.fhir.org/baseR4");

		IGenericClient client = builder.createFhirClient(simple);
		assertEquals(0, client.getInterceptorService().getAllRegisteredInterceptors().size());
	}
	
	@Test
	public void testAllFhirServerConfigOptions() throws Exception {
		FhirServerConfig config = new FhirServerConfig();
		config.setEndpoint("http://hapi.fhir.org/baseR4");
		config.setUser("fhiruser");
		config.setPassword("change-password");
		config.setToken("token");
		config.setCookies(Arrays.asList("ocookie=mycookie"));
		config.setHeaders(Collections.singletonMap("CUSTOM", "HEADER"));
		config.setLogInfo(Arrays.asList(LogInfo.values()));
		
		ObjectMapper mapper = new ObjectMapper();
		System.out.println( mapper.writeValueAsString(config) );
		
		IGenericClient client = builder.createFhirClient(config);
		assertEquals(5, client.getInterceptorService().getAllRegisteredInterceptors().size());
	}
	
	@Test
	public void testIBMFhirConfig() {
		IBMFhirServerConfig config = new IBMFhirServerConfig();
		config.setEndpoint("https://localhost:9443/fhir-server/api/v4");
		config.setUser("fhiruser");
		config.setPassword("change-password");
		config.setTenantIdHeader(IBMFhirServerConfig.DEFAULT_TENANT_ID_HEADER);
		config.setTenantId("default");
		config.setDataSourceIdHeader("DS-HEADER");
		config.setDataSourceId("datasource");

		FhirContext ctx = FhirContext.forR4();
		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(ctx);
		
		IGenericClient client = builder.createFhirClient(config);
		assertEquals(2, client.getInterceptorService().getAllRegisteredInterceptors().size());
	}
}
