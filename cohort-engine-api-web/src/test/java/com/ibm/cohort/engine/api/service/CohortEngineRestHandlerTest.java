/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2020                                      */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import com.ibm.cohort.engine.api.service.model.EvaluateMeasuresStatus;
import com.ibm.cohort.engine.api.service.model.MeasuresEvaluation;
import com.ibm.watson.common.service.base.ServiceBaseUtility;
import com.ibm.watson.common.service.base.security.Tenant;
import com.ibm.watson.common.service.base.security.TenantManager;

@RunWith(PowerMockRunner.class)

/**
 * Junit class to test the CohortEngineRestHandler.
 */

public class CohortEngineRestHandlerTest {
	EvaluateMeasuresStatus evaluateMeasuresStatus;
	String version;
	String methodName;
	Logger logger;
	HttpServletRequest requestContext;

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
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(Response.class);
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
		PowerMockito.when(Response.ok(Mockito.anyObject())).thenReturn(mockResponseBuilder);
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
		PowerMockito.when(Response.ok(Mockito.anyObject())).thenReturn(mockResponseBuilder);
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
		PowerMockito.when(Response.ok(Mockito.anyObject())).thenReturn(mockResponseBuilder);
		when(mockResponseBuilder.build()).thenReturn(mockResponse);
		Response loadResponse = restHandler.deleteEvaluation("version", "12345");
		assertEquals(mockResponse, loadResponse);
	}
}