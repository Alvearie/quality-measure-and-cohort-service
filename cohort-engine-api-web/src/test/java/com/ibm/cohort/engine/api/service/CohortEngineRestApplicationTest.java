/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.ibm.watson.common.service.base.ServiceExceptionMapper;
import com.ibm.watson.solutions.api.rest.CustomSwaggerApiListingResource2;

import io.swagger.jaxrs.listing.SwaggerSerializers;

/**
 * Junit class to test the CohortEngineRestApplication.
 */

public class CohortEngineRestApplicationTest {

	/**
	 * Test getClasses().
	 */

	@Test
	public void testGetClasses() throws Exception {
		CohortEngineRestApplication cohortApp = new CohortEngineRestApplication();
		Set<Class<?>> actualClasses = cohortApp.getClasses();
		Set<Class<?>> expectedClasses = new HashSet<>();
		expectedClasses.add(CohortEngineRestStatusHandler.class);
		expectedClasses.add(CohortEngineRestHandler.class);
		expectedClasses.add(CustomSwaggerApiListingResource2.class);
		expectedClasses.add(SwaggerSerializers.class);
		expectedClasses.add(ServiceExceptionMapper.class);
		assertEquals(expectedClasses, actualClasses);
	}

}