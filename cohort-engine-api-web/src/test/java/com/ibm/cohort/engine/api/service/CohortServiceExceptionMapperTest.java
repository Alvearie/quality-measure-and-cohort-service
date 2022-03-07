/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.ConnectException;

import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencds.cqf.cql.engine.exception.CqlException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.ibm.cohort.engine.api.service.model.ServiceErrorList;
import com.ibm.cohort.engine.api.service.model.ServiceErrorList.ErrorSource;
import com.ibm.watson.service.base.model.ServiceError;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;

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
		Response response = exMapper.toResponse(
				new IllegalArgumentException(
						"Something bad got input",
						new IllegalArgumentException("Nested exception")
				)
		);
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(400);
		expected.getErrors().add(newServiceError(400, "Something bad got input", ""));
		expected.getErrors().add(newServiceError(400, "Nested exception", null));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseUnsupportedOperationException() throws Exception {
		Response response = exMapper.toResponse(new UnsupportedOperationException("No support for that yet."));
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(400);
		expected.getErrors().add(newServiceError(400, "No support for that yet.", ""));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseCqlException() throws Exception {
		Response response = exMapper.toResponse(new CqlException("Unexpected exception caught during execution", new IllegalArgumentException("Failed to resolve ValueSet")));
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(400);
		expected.getErrors().add(newServiceError(400, "Unexpected exception caught during execution", ""));
		expected.getErrors().add(newServiceError(400, "Failed to resolve ValueSet", null));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}

	@Test
	public void testToResponseFhirClientConnectionException() throws Exception {
		Response response = exMapper.toResponse(new FhirClientConnectionException("Something bad got input"));
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(500);
		expected.getErrors().add(newServiceError(500, "Something bad got input", "Reason: FhirClientConnectionException"));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseFhirClientConnectionExceptionUnknownHost() throws Exception {
		Response response = exMapper.toResponse(new FhirClientConnectionException("Something bad got input").initCause(new java.net.UnknownHostException("bad host") ));
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(500);
		expected.getErrors().add(newServiceError(500, "Something bad got input", "Reason: FhirClientConnectionException"));
		expected.getErrors().add(newServiceError(404, "bad host", null));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseFhirClientConnectionExceptionUnknownHostConnectionRefused() throws Exception {
		Response response = exMapper.toResponse(new FhirClientConnectionException("Something bad got input").initCause(new java.net.ConnectException("connection refused") ));
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(500);
		expected.getErrors().add(newServiceError(500, "Something bad got input", "Reason: FhirClientConnectionException"));
		expected.getErrors().add(newServiceError(404, "connection refused", null));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseFhirClientConnectionExceptionUnknownHttpHostConnect() throws Exception {
		Response response = exMapper.toResponse(new FhirClientConnectionException("Something bad got input").initCause(new org.apache.http.conn.HttpHostConnectException(org.apache.http.HttpHost.create("host"), new ConnectException("cause")) ));
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(500);
		expected.getErrors().add(newServiceError(500, "Something bad got input", "Reason: FhirClientConnectionException"));
		expected.getErrors().add(newServiceError(404, "Connect to host failed: cause", null));
		expected.getErrors().add(newServiceError(500, "cause", null));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseFhirClientConnectionExceptionUnknownHostTimedOut() throws Exception {
		Response response = exMapper.toResponse(new FhirClientConnectionException("Something bad got input").initCause(new java.net.SocketTimeoutException("timed out") ));
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(500);
		expected.getErrors().add(newServiceError(500, "Something bad got input", "Reason: FhirClientConnectionException"));
		expected.getErrors().add(newServiceError(504, "timed out", null));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseFhirClientConnectionExceptionUnknownHostApacheTimedOut() throws Exception {
		Response response = exMapper.toResponse(new FhirClientConnectionException("Something bad got input").initCause(new org.apache.http.conn.ConnectTimeoutException("timed out") ));
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(500);
		expected.getErrors().add(newServiceError(500, "Something bad got input", "Reason: FhirClientConnectionException"));
		expected.getErrors().add(newServiceError(504, "timed out", null));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}

	@Test
	public void testToResponseAuthenticationException() throws Exception {
		Response response = exMapper.toResponse(new AuthenticationException());
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(400);
		expected.getErrors().add(newServiceError(401, "Client unauthorized", "Could not authenticate with FHIR server."));
		expected.setErrorSource(ErrorSource.FHIR_SERVER);

		testErrorListEquality(expected, actual);
	}

	@Test
	public void testToResponseNPE() throws Exception {
		Response response = exMapper.toResponse(new NullPointerException());
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(500);
		expected.getErrors().add(newServiceError(500, null, null));
		expected.setErrorSource(ErrorSource.COHORT_SERVICE);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseResourceNotFoundExceptionHAPIFormat() throws Exception {
		OperationOutcome outcome = new OperationOutcome();
		outcome.getText().setStatusAsString("generated");
		outcome.getIssueFirstRep()
				.setSeverity(IssueSeverity.ERROR)
				.setCode(OperationOutcome.IssueType.PROCESSING)
				.setDiagnostics("Resource Patient/something is not found");
		
		ResourceNotFoundException ex = new ResourceNotFoundException("Error", outcome);
		ex.setResponseBody(parser.encodeResourceToString(outcome));
		
		Response response = new CohortServiceExceptionMapper().toResponse(ex);
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(400);
		expected.getErrors().add(newServiceError(
				404,
				"FHIR Resource Not Found: Error",
				"{\"resourceType\":\"OperationOutcome\",\"text\":{\"status\":\"generated\"},\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Resource Patient/something is not found\"}]}"
		));
		expected.setErrorSource(ErrorSource.FHIR_SERVER);

		testErrorListEquality(expected, actual);
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
		json.put("id", "my-id");
		json.put("issue", issues);

		OperationOutcome outcome = parser.parseResource(OperationOutcome.class, json.toString());

		ResourceNotFoundException ex = new ResourceNotFoundException("Error", outcome);
		ex.setResponseBody(json.toString());
		
		Response response = new CohortServiceExceptionMapper().toResponse(ex);
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(400);
		expected.getErrors().add(newServiceError(
				404,
				"FHIR Resource Not Found: Error",
				"{\"issue\":[{\"severity\":\"fatal\",\"code\":\"not-found\",\"details\":{\"text\":\"Resource 'Patient\\/patientId' not found.\"}}],\"id\":\"my-id\",\"resourceType\":\"OperationOutcome\"}"
		));
		expected.setErrorSource(ErrorSource.FHIR_SERVER);

		testErrorListEquality(expected, actual);
	}
	
	@Test
	public void testToResponseResourceNotFoundExceptionNotOperationOutcome() throws Exception {
		ResourceNotFoundException ex = new ResourceNotFoundException("Bad URL");
		ex.setResponseBody("Bad Body");

		Response response = new CohortServiceExceptionMapper().toResponse(ex);
		ServiceErrorList actual = (ServiceErrorList) response.getEntity();

		ServiceErrorList expected = new ServiceErrorList();
		expected.setStatusCode(400);
		expected.getErrors().add(newServiceError(
				404,
				"FHIR Resource Not Found: Bad URL",
				"Bad Body"
		));
		expected.setErrorSource(ErrorSource.FHIR_SERVER);

		testErrorListEquality(expected, actual);
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

	private ServiceError newServiceError(int code, String message, String description) {
		ServiceError retVal = new ServiceError(code, message);
		retVal.setDescription(description);
		return retVal;
	}

	/*
	 * We have to compare the serialized form of the objects due to `ServiceError`
	 * and `ServiceErrorList` lacking a hashCode and equals method.
	 */
	private void testErrorListEquality(ServiceErrorList expected, ServiceErrorList actual) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String expectedString = mapper.writeValueAsString(expected);
		String actualString = mapper.writeValueAsString(actual);
		assertEquals(expectedString, actualString);
	}
}
