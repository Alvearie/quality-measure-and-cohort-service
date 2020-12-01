/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
/*

* Junit class to test Service Initialization.
*/

public class CohortEngineRestInitializationTest {
	@Mock
	private static ServletContextEvent mockServletContextEvent;
	@Mock
	private static ServletContext mockServletContext;
	@InjectMocks
	private static CohortEngineRestInitialization iMockServiceInitialization;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test Service Initialization settings.
	 */
	@Test
	public void testServiceInitialization() throws Exception {
		when(mockServletContextEvent.getServletContext()).thenReturn(mockServletContext);
		when(mockServletContextEvent.getServletContext().getContextPath()).thenReturn("contextRoot");
		try {
			iMockServiceInitialization.contextInitialized(mockServletContextEvent);
			fail("Expecting NPE for serviceInit.contextInitialized.");
		} catch (Exception ex) {
			assert (ex instanceof NullPointerException);
		}
		assertEquals(CohortEngineRestConstants.SERVICE_TITLE, iMockServiceInitialization.getServiceTitle());
		assertEquals(CohortEngineRestConstants.SERVICE_DESCRIPTION, iMockServiceInitialization.getServiceDescription());
		assertEquals(CohortEngineRestConstants.SERVICE_SWAGGER_PACKAGES,
				iMockServiceInitialization.getSwaggerPackages());
	}
}