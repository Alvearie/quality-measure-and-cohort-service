/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.ibm.watson.service.base.model.ServiceStatus;
import com.ibm.watson.service.base.model.ServiceStatus.ServiceState;

public class CohortEngineRestStatusHandlerTest {

	@InjectMocks
	private static CohortEngineRestStatusHandler cerSH;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

}
