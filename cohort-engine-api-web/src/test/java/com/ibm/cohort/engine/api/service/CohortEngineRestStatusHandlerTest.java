/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.agent.PowerMockAgent;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.engine.api.service.model.EnhancedHealthCheckResults;
import com.ibm.cohort.engine.api.service.model.FhirServerConnectionStatusInfo.FhirConnectionStatus;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilder;
import com.ibm.watson.common.service.base.ServiceBaseUtility;
import com.ibm.watson.common.service.base.security.TenantManager;
import com.ibm.watson.service.base.model.ServiceStatus;
import com.ibm.watson.service.base.model.ServiceStatus.ServiceState;

public class CohortEngineRestStatusHandlerTest extends CohortHandlerBaseTest {
	// Need to add below to get jacoco to work with powermockito
	@Rule
	public PowerMockRule rule = new PowerMockRule();
	static {
		PowerMockAgent.initializeIfNeeded();
	}
	
	// We're using this to mock calls to CohortEngineRestStatusHandler, thus to make the mocking work, we need to use the same logger as that class
	private static final Logger logger = LoggerFactory.getLogger(CohortEngineRestStatusHandler.class.getName());
	private static final String VERSION = "version";
		
	@InjectMocks
	private static CohortEngineRestStatusHandler cerSH;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		BaseFhirTest.setUpBeforeClass();
		cerSH = new CohortEngineRestStatusHandler();
	}

	@Test
	/**
	 * Test the setting of service status to OK.
	 */
	public void testAdjustServiceStatusOK() throws Exception {
		ServiceStatus status = new ServiceStatus();
		ServiceStatus newStatus = null;

		// Update service status according to mocked health check.
		newStatus = cerSH.adjustServiceStatus(status);
		assertEquals("The service state was not OK.", ServiceState.OK, newStatus.getServiceState());
	}
	
	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class })
	@Test
	/**
	 * Test the setting of service status to OK.
	 */
	public void testEnhancedHealthCheckOK() throws Exception {
		prepMocks(false);
		
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(VERSION, logger, CohortEngineRestStatusHandler.GET_HEALTH_CHECK_ENCHANCED)).thenReturn(null);
		
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata?_format=json", metadata);
		
		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval("/Patient?_format=json", patient);
		
		//don't need to return a valueset since we only care that the call returns something
		mockFhirResourceRetrieval("/ValueSet?_format=json", patient);
		
		Response response = cerSH.getHealthCheckEnhanced(VERSION, getEnhancedHealthCheckInputConfigFileBody(true));
		
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertEquals(((EnhancedHealthCheckResults)response.getEntity()).getDataServerConnectionResults().getConnectionResults(), FhirConnectionStatus.success);
		assertEquals(((EnhancedHealthCheckResults)response.getEntity()).getTerminologyServerConnectionResults().getConnectionResults(), FhirConnectionStatus.success);
	}
	
	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class })
	@Test
	/**
	 * Test the service returns a failure when trying to connect to data and terminology servers
	 */
	public void testEnhancedHealthCheckFailed() throws Exception {
		prepMocks(false);
		
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(VERSION, logger, CohortEngineRestStatusHandler.GET_HEALTH_CHECK_ENCHANCED)).thenReturn(null);
		
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata?_format=json", metadata);
		
		//return null to cause exception
		mockFhirResourceRetrieval("/Patient?_format=json", null);
		mockFhirResourceRetrieval("/ValueSet?_format=json", null);
		
		Response response = cerSH.getHealthCheckEnhanced(VERSION, getEnhancedHealthCheckInputConfigFileBody(true));
		
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertEquals(((EnhancedHealthCheckResults)response.getEntity()).getDataServerConnectionResults().getConnectionResults(), FhirConnectionStatus.failure);
		assertEquals(((EnhancedHealthCheckResults)response.getEntity()).getTerminologyServerConnectionResults().getConnectionResults(), FhirConnectionStatus.failure);
	}
	
	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class })
	@Test
	/**
	 * Test a failure when invalid input provided
	 */
	public void testEnhancedHealthCheckFailedNoInput() throws Exception {
		prepMocks(false);
		
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(VERSION, logger, CohortEngineRestStatusHandler.GET_HEALTH_CHECK_ENCHANCED)).thenReturn(null);
		
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata?_format=json", metadata);
		
		//return null to cause exception
		mockFhirResourceRetrieval("/Patient?_format=json", null);
		mockFhirResourceRetrieval("/ValueSet?_format=json", null);
		
		Response response = cerSH.getHealthCheckEnhanced(VERSION, null);
		
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}
	
	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class })
	@Test
	/**
	 * Test a failure connecting to data server and no terminology server provided
	 */
	public void testEnhancedHealthCheckFailNoTermServer() throws Exception {
		prepMocks(false);
		
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(VERSION, logger, CohortEngineRestStatusHandler.GET_HEALTH_CHECK_ENCHANCED)).thenReturn(null);
		
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata?_format=json", metadata);
		
		mockFhirResourceRetrieval("/Patient?_format=json", null);
		
		Response response = cerSH.getHealthCheckEnhanced(VERSION, getEnhancedHealthCheckInputConfigFileBody(false));
		
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertEquals(((EnhancedHealthCheckResults)response.getEntity()).getDataServerConnectionResults().getConnectionResults(), FhirConnectionStatus.failure);
		assertEquals(((EnhancedHealthCheckResults)response.getEntity()).getTerminologyServerConnectionResults().getConnectionResults(), FhirConnectionStatus.notAttempted);
	}
	
	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class })
	@Test
	public void testEnhancedHealthCheckOKNoTermServer() throws Exception {
		prepMocks(false);
		
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(VERSION, logger, CohortEngineRestStatusHandler.GET_HEALTH_CHECK_ENCHANCED)).thenReturn(null);	
		
		CapabilityStatement metadata = getCapabilityStatement();
		mockFhirResourceRetrieval("/metadata?_format=json", metadata);
		
		Patient patient = getPatient("123", AdministrativeGender.MALE, "1970-10-10");
		mockFhirResourceRetrieval("/Patient?_format=json", patient);
		
		Response response = cerSH.getHealthCheckEnhanced(VERSION, getEnhancedHealthCheckInputConfigFileBody(false));
		
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertEquals(((EnhancedHealthCheckResults)response.getEntity()).getDataServerConnectionResults().getConnectionResults(), FhirConnectionStatus.success);
		assertEquals(((EnhancedHealthCheckResults)response.getEntity()).getTerminologyServerConnectionResults().getConnectionResults(), FhirConnectionStatus.notAttempted);
	}
	

}
