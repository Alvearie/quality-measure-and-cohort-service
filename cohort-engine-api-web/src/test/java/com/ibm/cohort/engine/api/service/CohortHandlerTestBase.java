/*
 *
 *  * (C) Copyright IBM Corp. 2022
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.ibm.cohort.engine.api.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.FhirTestBase;
import com.ibm.cohort.engine.api.service.model.EnhancedHealthCheckInput;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilder;
import com.ibm.watson.common.service.base.ServiceBaseUtility;
import com.ibm.watson.common.service.base.security.Tenant;
import com.ibm.watson.common.service.base.security.TenantManager;
import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;

public class CohortHandlerTestBase extends FhirTestBase {

	@Mock
	protected static HttpServletRequest mockRequestContext;
	@Mock
	protected static HttpHeaders mockHttpHeaders;
	@Mock
	protected static DefaultFhirClientBuilder mockDefaultFhirClientBuilder;
	
	protected List<String> httpHeadersList = Arrays.asList("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
	
	public CohortHandlerTestBase() {
		// TODO Auto-generated constructor stub
	}

	
	protected void prepMocks() {
		prepMocks(true);
	}

	protected void prepMocks(boolean prepResponse) {
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		if (prepResponse) {
			PowerMockito.mockStatic(Response.class);
		}
		PowerMockito.mockStatic(TenantManager.class);

		PowerMockito.when(TenantManager.getTenant()).thenReturn(new Tenant() {
			@Override
			public String getTenantId() {
				return "JunitTenantId";
			}

			@Override
			public String getUserId() {
				return "JunitUserId";
			}

		});
		when(mockRequestContext.getRemoteAddr()).thenReturn("1.1.1.1");
		when(mockRequestContext.getLocalAddr()).thenReturn("1.1.1.1");
		when(mockRequestContext.getRequestURL())
				.thenReturn(new StringBuffer("http://localhost:9080/services/cohort/api/v1/evaluation"));
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(httpHeadersList);
		when(mockDefaultFhirClientBuilder.createFhirClient(ArgumentMatchers.any())).thenReturn(null);
	}
	
	protected IMultipartBody getEnhancedHealthCheckInputConfigFileBody(boolean includeTerminologyServer) throws Exception {
		// Create the metadata part of the request
		ObjectMapper om = new ObjectMapper();
		
		EnhancedHealthCheckInput input = new EnhancedHealthCheckInput();
		input.setDataServerConfig(getFhirServerConfig());
		if (includeTerminologyServer) {
			input.setTerminologyServerConfig(getFhirServerConfig());
		}
		
		String json = om.writeValueAsString(input);
		ByteArrayInputStream jsonIs = new ByteArrayInputStream(json.getBytes());
		IAttachment rootPart = mockAttachment(jsonIs);
		
		// Assemble them together into a reasonable facsimile of the real request
		IMultipartBody body = mock(IMultipartBody.class);
		when( body.getAttachment(CohortEngineRestStatusHandler.FHIR_SERVER_CONNECTION_CONFIG) ).thenReturn(rootPart);
		
		return body;
	}
	
	protected IAttachment mockAttachment(InputStream multipartData) throws IOException {
		IAttachment measurePart = mock(IAttachment.class);
		DataHandler zipHandler = mock(DataHandler.class);
		when( zipHandler.getInputStream() ).thenReturn(multipartData);
		when( measurePart.getDataHandler() ).thenReturn( zipHandler );
		return measurePart;
	}
}
