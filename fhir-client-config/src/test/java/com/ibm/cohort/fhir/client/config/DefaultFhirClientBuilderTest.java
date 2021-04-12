/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.fhir.client.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.fhir.client.config.FhirServerConfig.LogInfo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;

public class DefaultFhirClientBuilderTest {
	
	static FhirContext ctx;
	DefaultFhirClientBuilder builder;

	private static final int SOCKET_TIMEOUT = 1;
	private static final int CONNECT_TIMEOUT = 2;
	private static final int CONNECTION_REQUEST_TIMEOUT = 3;
	private static final int NEGATIVE_TIMEOUT = -10;

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

	@Test
	public void testAllTimeoutConfigs() {
		FhirServerConfig config = new FhirServerConfig();
		config.setSocketTimeout(SOCKET_TIMEOUT);
		config.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT);
		config.setConnectTimeout(CONNECT_TIMEOUT);

		IRestfulClientFactory mockClientFactory = mock(IRestfulClientFactory.class);

		FhirContext mockContext = mock(FhirContext.class);
		when(mockContext.getRestfulClientFactory()).thenReturn(mockClientFactory);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(mockContext);

		builder.createFhirClient(config);

		verify(mockClientFactory, times(1)).setSocketTimeout(SOCKET_TIMEOUT);
		verify(mockClientFactory, times(1)).setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT);
		verify(mockClientFactory, times(1)).setConnectTimeout(CONNECT_TIMEOUT);

		verify(mockClientFactory, times(0)).setSocketTimeout(IRestfulClientFactory.DEFAULT_SOCKET_TIMEOUT);
		verify(mockClientFactory, times(0)).setConnectionRequestTimeout(IRestfulClientFactory.DEFAULT_CONNECTION_REQUEST_TIMEOUT);
		verify(mockClientFactory, times(0)).setConnectTimeout(IRestfulClientFactory.DEFAULT_CONNECT_TIMEOUT);
	}

	@Test
	public void testNoTimeoutConfigs() {
		FhirServerConfig config = new FhirServerConfig();

		IRestfulClientFactory mockClientFactory = mock(IRestfulClientFactory.class);

		FhirContext mockContext = mock(FhirContext.class);
		when(mockContext.getRestfulClientFactory()).thenReturn(mockClientFactory);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(mockContext);

		builder.createFhirClient(config);

		verify(mockClientFactory, times(1)).setSocketTimeout(IRestfulClientFactory.DEFAULT_SOCKET_TIMEOUT);
		verify(mockClientFactory, times(1)).setConnectionRequestTimeout(IRestfulClientFactory.DEFAULT_CONNECTION_REQUEST_TIMEOUT);
		verify(mockClientFactory, times(1)).setConnectTimeout(IRestfulClientFactory.DEFAULT_CONNECT_TIMEOUT);
	}

	@Test
	public void testReuseBuilderMultipleConfigs() {
		FhirServerConfig socketTimeoutConfig = new FhirServerConfig();
		socketTimeoutConfig.setSocketTimeout(SOCKET_TIMEOUT);

		FhirServerConfig connectTimeoutConfig = new FhirServerConfig();
		connectTimeoutConfig.setConnectTimeout(CONNECT_TIMEOUT);

		FhirServerConfig connectionRequestTimeoutConfig = new FhirServerConfig();
		connectionRequestTimeoutConfig.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT);

		IRestfulClientFactory mockClientFactory = mock(IRestfulClientFactory.class);

		FhirContext mockContext = mock(FhirContext.class);
		when(mockContext.getRestfulClientFactory()).thenReturn(mockClientFactory);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(mockContext);

		builder.createFhirClient(socketTimeoutConfig);
		builder.createFhirClient(connectTimeoutConfig);
		builder.createFhirClient(connectionRequestTimeoutConfig);

		verify(mockClientFactory, times(1)).setSocketTimeout(SOCKET_TIMEOUT);
		verify(mockClientFactory, times(1)).setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT);
		verify(mockClientFactory, times(1)).setConnectTimeout(CONNECT_TIMEOUT);

		verify(mockClientFactory, times(2)).setSocketTimeout(IRestfulClientFactory.DEFAULT_SOCKET_TIMEOUT);
		verify(mockClientFactory, times(2)).setConnectionRequestTimeout(IRestfulClientFactory.DEFAULT_CONNECTION_REQUEST_TIMEOUT);
		verify(mockClientFactory, times(2)).setConnectTimeout(IRestfulClientFactory.DEFAULT_CONNECT_TIMEOUT);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeSocketTimeout() {
		FhirServerConfig fhirServerConfig = new FhirServerConfig();
		fhirServerConfig.setSocketTimeout(NEGATIVE_TIMEOUT);

		IRestfulClientFactory mockClientFactory = mock(IRestfulClientFactory.class);

		FhirContext mockContext = mock(FhirContext.class);
		when(mockContext.getRestfulClientFactory()).thenReturn(mockClientFactory);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(mockContext);

		builder.createFhirClient(fhirServerConfig);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeConnectTimeout() {
		FhirServerConfig fhirServerConfig = new FhirServerConfig();
		fhirServerConfig.setConnectTimeout(NEGATIVE_TIMEOUT);

		IRestfulClientFactory mockClientFactory = mock(IRestfulClientFactory.class);

		FhirContext mockContext = mock(FhirContext.class);
		when(mockContext.getRestfulClientFactory()).thenReturn(mockClientFactory);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(mockContext);

		builder.createFhirClient(fhirServerConfig);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeConnectionRequestTimeout() {
		FhirServerConfig fhirServerConfig = new FhirServerConfig();
		fhirServerConfig.setConnectionRequestTimeout(NEGATIVE_TIMEOUT);

		IRestfulClientFactory mockClientFactory = mock(IRestfulClientFactory.class);

		FhirContext mockContext = mock(FhirContext.class);
		when(mockContext.getRestfulClientFactory()).thenReturn(mockClientFactory);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(mockContext);

		builder.createFhirClient(fhirServerConfig);
	}
}
