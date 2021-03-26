/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.ibm.cohort.engine.api.service.model.ServiceErrorList;
import com.ibm.cohort.engine.parameter.Parameter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * Junit class to test the CohortServiceExceptionMapperTest.
 */

public class CohortServiceExceptionMapperTest {
	private static CohortServiceExceptionMapper exMapper;
	private static IParser parser;
	
	@Before
	public void setUp() throws Exception {
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		exMapper = new CohortServiceExceptionMapper();
		parser = FhirContext.forR4().newJsonParser();
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
	
	@Test
	public void testToResponseResourceNotFoundExceptionHAPIFormat() throws Exception {
		OperationOutcome outcome = new OperationOutcome();
		outcome.getText().setStatusAsString("generated");
		outcome.getIssueFirstRep().setSeverity(IssueSeverity.ERROR).setCode(OperationOutcome.IssueType.PROCESSING).setDiagnostics("Resource Patient/something is not found");
		
		ResourceNotFoundException ex = new ResourceNotFoundException("Error", outcome);
		ex.setResponseBody(parser.encodeResourceToString(outcome));
		
		Response response = new CohortServiceExceptionMapper().toResponse(ex);
		ServiceErrorList serviceErrorList = (ServiceErrorList) response.getEntity();
		assertEquals(new Integer(400), serviceErrorList.getStatusCode());
		assertEquals(1, serviceErrorList.getErrors().size());
		
		//System.out.println(serviceErrorList.getErrors().get(0).getDescription());
		
		assertTrue( "Missing resource ID in response", serviceErrorList.getErrors().get(0).getDescription().contains("Patient/something") );
	}
	
	@Test
	public void testToResponseResourceNotFoundExceptionIBMFormat() throws Exception {
		JSONObject details = new JSONObject();
		details.put("text", "Resource 'Patient/patientId' not found.");
		
		JSONObject issue = new JSONObject();
		issue.put("severity", "fatal");
		issue.put("code", "not-found");
		issue.put("details", details);
		
		JSONArray issues = new JSONArray();
		issues.add(issue);
		
		JSONObject json = new JSONObject();
		json.put("resourceType", "OperationOutcome");
		json.put("id", UUID.randomUUID().toString());
		json.put("issue", issues);

		OperationOutcome outcome = parser.parseResource(OperationOutcome.class, json.toString());
		
		ResourceNotFoundException ex = new ResourceNotFoundException("Error", outcome);
		ex.setResponseBody(json.toString());
		
		Response response = new CohortServiceExceptionMapper().toResponse(ex);
		ServiceErrorList serviceErrorList = (ServiceErrorList) response.getEntity();
		assertEquals(new Integer(400), serviceErrorList.getStatusCode());
		assertEquals(1, serviceErrorList.getErrors().size());
		
		//System.out.println("---" + serviceErrorList.getErrors().get(0).getDescription());
		
		assertTrue( "Missing resource ID in response", serviceErrorList.getErrors().get(0).getDescription().contains("patientId") );
	}
	
	@Test
	public void testToResponseResourceNotFoundExceptionNotOperationOutcome() throws Exception {
		
		Response response = new CohortServiceExceptionMapper().toResponse(new ResourceNotFoundException("Bad URL"));
		ServiceErrorList serviceErrorList = (ServiceErrorList) response.getEntity();
		assertEquals(new Integer(400), serviceErrorList.getStatusCode());
		assertEquals(1, serviceErrorList.getErrors().size());
		
		//System.out.println("---" + serviceErrorList.getErrors().get(0).getDescription());
		
		assertTrue( "Missing resource ID in response", serviceErrorList.getErrors().get(0).getDescription().contains("Bad URL") );
	}
	
	@Test
	public void testInvalidParameterJSON() throws Exception {
		JSONObject start = new JSONObject();
		start.put("type", "datetime");
		start.put("value", "@2020-02-04T05:06:07.0");
		
		JSONObject end = new JSONObject();
		end.put("type", "datetime");
		end.put("value", "@2021-02-04T05:06:07.0");
		
		JSONObject param = new JSONObject();
		param.put("type", "interval");
		param.put("starttime", start);
		param.put("end", end);
		
		System.out.println("\n" + param.toString());
		
		try {
			ObjectMapper om = new ObjectMapper();
			/*Parameter p = */ om.readValue(param.toString(), Parameter.class);
			fail("Deserialization should have failed");
		} catch( UnrecognizedPropertyException ex ) {
			Response response = exMapper.toResponse(ex);
			assertEquals(400, response.getStatus());
		}
	}
}