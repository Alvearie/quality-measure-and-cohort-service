/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.cohort.engine.api.service.model.ServiceErrorList;

import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;

/**
 * Junit class to test the CohortServiceExceptionMapperTest.
 */

public class CohortServiceExceptionMapperTest {
	private static CohortServiceExceptionMapper exMapper;

	@Before
	public void setUp() throws Exception {
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		exMapper = new CohortServiceExceptionMapper();
	}

	@Test
	public void testToResponseIllegalArgumentException() throws Exception {
		Response response = exMapper.toResponse(new IllegalArgumentException("Something bad got input",
				new IllegalArgumentException("Nested exception")));
		ServiceErrorList serviceErrorList = (ServiceErrorList) response.getEntity();
		assertEquals(new Integer(400), serviceErrorList.getStatusCode());
		assertEquals(2, serviceErrorList.getErrors().size());
		assertEquals("Something bad got input", serviceErrorList.getErrors().get(0).getMessage());
		assertEquals("Nested exception", serviceErrorList.getErrors().get(1).getMessage());
	}

	@Test
	public void testToResponseFhirClientConnectionException() throws Exception {
		Response response = exMapper.toResponse(new FhirClientConnectionException("Something bad got input"));
		ServiceErrorList serviceErrorList = (ServiceErrorList) response.getEntity();
		assertEquals(new Integer(400), serviceErrorList.getStatusCode());
		assertEquals(1, serviceErrorList.getErrors().size());
		assertEquals("Something bad got input", serviceErrorList.getErrors().get(0).getMessage());
		assertEquals("Reason: FhirClientConnectionException", serviceErrorList.getErrors().get(0).getDescription());
	}

	@Test
	public void testToResponseAuthenticationException() throws Exception {
		Response response = exMapper.toResponse(new AuthenticationException());
		ServiceErrorList serviceErrorList = (ServiceErrorList) response.getEntity();
		assertEquals(new Integer(400), serviceErrorList.getStatusCode());
		assertEquals(1, serviceErrorList.getErrors().size());
		assertEquals("Client unauthorized", serviceErrorList.getErrors().get(0).getMessage());
		assertEquals("Could not authenticate with FHIR server.", serviceErrorList.getErrors().get(0).getDescription());
		assertEquals(401, serviceErrorList.getErrors().get(0).getCode());
	}

	@Test
	public void testToResponseNPE() throws Exception {
		Response response = exMapper.toResponse(new NullPointerException());
		ServiceErrorList serviceErrorList = (ServiceErrorList) response.getEntity();
		assertEquals(new Integer(500), serviceErrorList.getStatusCode());
		assertEquals(1, serviceErrorList.getErrors().size());
	}
}