/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.agent.PowerMockAgent;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.engine.api.service.model.EvaluateMeasuresStatus;
import com.ibm.cohort.engine.api.service.model.MeasureParameterInfo;
import com.ibm.cohort.engine.api.service.model.MeasuresEvaluation;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilder;
import com.ibm.watson.common.service.base.ServiceBaseUtility;
import com.ibm.watson.common.service.base.security.Tenant;
import com.ibm.watson.common.service.base.security.TenantManager;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Junit class to test the CohortEngineRestHandler.
 */

public class CohortEngineRestHandlerTest {
	// Need to add below to get jacoco to work with powermockito
	@Rule
	public PowerMockRule rule = new PowerMockRule();
	static {
		PowerMockAgent.initializeIfNeeded();
	}

	private static final Logger logger = LoggerFactory.getLogger(CohortEngineRestHandlerTest.class.getName());
	EvaluateMeasuresStatus evaluateMeasuresStatus;
	String version;
	String methodName;
	HttpServletRequest requestContext;
	IGenericClient measureClient;
	List<String> httpHeadersList = Arrays.asList("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
	String[] authParts = new String[] { "username", "password" };
	List<MeasureParameterInfo> parameterInfoList = new ArrayList<MeasureParameterInfo>(
			Arrays.asList(new MeasureParameterInfo().documentation("documentation").name("name").min(0).max("max")
					.use("IN").type("type")));

	@Mock
	private static DefaultFhirClientBuilder mockDefaultFhirClientBuilder;
	@Mock
	private static HttpHeaders mockHttpHeaders;
	@Mock
	private static HttpServletRequest mockRequestContext;
	@Mock
	private static Response mockResponse;
	@Mock
	private static ResponseBuilder mockResponseBuilder;
	@Mock
	private static EvaluateMeasuresStatus mockEvaluateMeasuresStatus;
	@InjectMocks
	private static CohortEngineRestHandler restHandler;

	private void prepMocks() {
		prepMocks(true);
	}

	private void prepMocks(boolean prepResponse) {
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

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		restHandler = new CohortEngineRestHandler();
	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class })
	@Test
	public void testEvaluateMeasuresStub() throws Exception {
		prepMocks();
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(Response.class);
		PowerMockito.mockStatic(ResponseBuilder.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(Response.status(Response.Status.ACCEPTED)).thenReturn(mockResponseBuilder);
		PowerMockito.when(mockResponseBuilder.entity(Mockito.anyString())).thenReturn(mockResponseBuilder);
		PowerMockito.when(mockResponseBuilder.header(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(mockResponseBuilder);
		when(mockResponseBuilder.build()).thenReturn(mockResponse);
		Response loadResponse = restHandler.evaluateMeasures(mockRequestContext, "version", new MeasuresEvaluation());
		assertEquals(mockResponse, loadResponse);

	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class, EvaluateMeasuresStatus.class })
	@Test
	public void testGetEvaluateMeasuresStatusStub() throws Exception {
		prepMocks();
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(Response.class);
		PowerMockito.mockStatic(ResponseBuilder.class);
		PowerMockito.mockStatic(EvaluateMeasuresStatus.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(Response.ok(ArgumentMatchers.any())).thenReturn(mockResponseBuilder);
		when(mockResponseBuilder.build()).thenReturn(mockResponse);
		Response loadResponse = restHandler.getEvaluateMeasuresStatus("version", "12345");
		assertEquals(mockResponse, loadResponse);
	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class, EvaluateMeasuresStatus.class })
	@Test
	public void testGetEvaluateMeasuresResultsStub() throws Exception {
		prepMocks();
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(Response.class);
		PowerMockito.mockStatic(ResponseBuilder.class);
		PowerMockito.mockStatic(EvaluateMeasuresStatus.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(Response.ok(ArgumentMatchers.any())).thenReturn(mockResponseBuilder);
		when(mockResponseBuilder.build()).thenReturn(mockResponse);
		Response loadResponse = restHandler.getEvaluateMeasuresResults("version", "12345");
		assertEquals(mockResponse, loadResponse);
	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class })
	@Test
	public void testDeleteEvaluationStub() throws Exception {
		prepMocks();
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(Response.class);
		PowerMockito.mockStatic(ResponseBuilder.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(Response.ok(ArgumentMatchers.any())).thenReturn(mockResponseBuilder);
		when(mockResponseBuilder.build()).thenReturn(mockResponse);
		Response loadResponse = restHandler.deleteEvaluation("version", "12345");
		assertEquals(mockResponse, loadResponse);
	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class,
			FHIRRestUtils.class })
	@Test
	public void testGetMeasureParameters() throws Exception {
		prepMocks(false);
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(FHIRRestUtils.class);
		PowerMockito.mockStatic(DefaultFhirClientBuilder.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(FHIRRestUtils.parseAuthenticationHeaderInfo(ArgumentMatchers.any())).thenReturn(authParts);
		PowerMockito.when(
				FHIRRestUtils.getFHIRClient(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
						ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(measureClient);
		PowerMockito.when(FHIRRestUtils.getParametersForMeasureIdentifier(ArgumentMatchers.any(),
				ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(parameterInfoList);

		Response loadResponse = restHandler.getMeasureParameters(mockHttpHeaders, "version", "fhirEndpoint",
				"fhirTenantId", "measureIdentifierValue", "measureIdentifierSystem", "measureVersion",
				"fhirTenantIdHeader", "fhirDataSourceIdHeader", "fhirDataSourceId");
		validateParameterResponse(loadResponse);
	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class,
			FHIRRestUtils.class })
	@Test
	public void testGetMeasureParametersById() throws Exception {
		prepMocks(false);
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(FHIRRestUtils.class);
		PowerMockito.mockStatic(DefaultFhirClientBuilder.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(FHIRRestUtils.parseAuthenticationHeaderInfo(ArgumentMatchers.any())).thenReturn(authParts);
		PowerMockito.when(
				FHIRRestUtils.getFHIRClient(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
						ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(measureClient);
		PowerMockito.when(FHIRRestUtils.getParametersForMeasureId(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(parameterInfoList);

		Response loadResponse = restHandler.getMeasureParametersById(mockHttpHeaders, "version", "fhirEndpoint",
				"fhirTenantId", "measureId", "fhirTenantIdHeader", "fhirDataSourceIdHeader", "fhirDataSourceId");
		validateParameterResponse(loadResponse);
	}

	private void validateParameterResponse(Response loadResponse) {
		assertEquals(Status.OK.getStatusCode(), loadResponse.getStatus());
		String validResp = "class MeasureParameterInfoList {\n"
				+ "    parameterInfoList: [class MeasureParameterInfo {\n" + "        name: name\n"
				+ "        use: IN\n" + "        min: 0\n" + "        max: max\n" + "        type: type\n"
				+ "        defaultValue: null\n"
				+ "        documentation: documentation\n" + "    ]\n" + "}";
		assertEquals(validResp, loadResponse.getEntity().toString());
	}
}