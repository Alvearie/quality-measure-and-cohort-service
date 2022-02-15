/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.ibm.cohort.engine.api.service.model.ServiceErrorList.ErrorSource;
import org.opencds.cqf.cql.engine.exception.CqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.ibm.cohort.engine.api.service.model.ServiceErrorList;
import com.ibm.watson.service.base.model.ServiceError;

import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

@Provider
public class CohortServiceExceptionMapper implements ExceptionMapper<Throwable>{
	private static final Logger logger = LoggerFactory.getLogger(CohortServiceExceptionMapper.class.getName());
	private static final int MAX_EXCEPTION_CAUSE_DEPTH = 20;

	@Override
	public Response toResponse(Throwable ex) {
		ServiceErrorList serviceErrorList = toServiceErrorList(ex);

		ResponseBuilder rb = Response.status(serviceErrorList.getStatusCode()).
				entity(serviceErrorList).
				type(MediaType.APPLICATION_JSON);

		return rb.build();
	}
	

	public ServiceErrorList toServiceErrorList(Throwable ex) {
		List<ServiceError> errorsList = new ArrayList<>();
		//The IBM Cloud API Handbook mandates that REST errors be returned using
		//an error container model class (ServiceErrorList) which in turn contains
		//a list of error objects (ServiceError) which contains specific error fields
		//serviceErrorList contains the status request (ie 400, 500 etc.) for our service
		//and the status code for underlying services is captured in the serviceError objects
		//within the list
		ServiceErrorList serviceErrorList = new ServiceErrorList().errors(errorsList);
		ServiceError se;
		String description = "";
		String reason = "";
		int serviceErrorCode = 500;
		int serviceErrorListCode = 500;
		ErrorSource errorSource;

		try {
			if (ex instanceof FhirClientConnectionException) {
				FhirClientConnectionException fcce = (FhirClientConnectionException) ex;
				serviceErrorCode = fcce.getStatusCode();
				//serviceErrorCode should be set by the underlying code,
				//but often it is not, so check and set it to a 400 error
				//if something more specific is not returned
				if(serviceErrorCode == 0) {
					serviceErrorCode = Status.BAD_REQUEST.getStatusCode();
				}
				serviceErrorListCode = serviceErrorCode;
				
				Status status = Status.fromStatusCode(serviceErrorCode);
				reason = fcce.getLocalizedMessage();
				if (reason == null || reason.trim().isEmpty()) {
					reason = status.getReasonPhrase();
				}
				description = "Reason: FhirClientConnectionException";
				errorSource = ErrorSource.COHORT_SERVICE;
			}
			//if we fail to authenticate with the FHIR server, a 401 Not authorized error
			//is thrown from the FHIR client. We set our error code to 400 since they are not technically
			//failing authentication with our server, but rather an underlying service.
			//The 401 error code information is captured in the ServiceError object
			else if (ex instanceof AuthenticationException) {
				AuthenticationException ae = (AuthenticationException) ex;
				serviceErrorListCode = Status.BAD_REQUEST.getStatusCode();
				serviceErrorCode = ae.getStatusCode();
				description = "Could not authenticate with FHIR server.";
				errorSource = ErrorSource.FHIR_SERVER;
			}
			else if (ex instanceof ResourceNotFoundException) {
				ResourceNotFoundException rnfe = (ResourceNotFoundException) ex;
				serviceErrorListCode = Status.BAD_REQUEST.getStatusCode();
				serviceErrorCode = rnfe.getStatusCode();
				reason = "FHIR Resource Not Found: " + rnfe.getLocalizedMessage();
				description = rnfe.getResponseBody();
				errorSource = ErrorSource.FHIR_SERVER;
			}
			//will get thrown is invalid measure ids are input or
			//library ids don't resolve properly
			else if (ex instanceof IllegalArgumentException || ex instanceof UnsupportedOperationException){
				serviceErrorCode = Status.BAD_REQUEST.getStatusCode();
				serviceErrorListCode = serviceErrorCode;
				errorSource = ErrorSource.COHORT_SERVICE;
			}
			//will get thrown by the CQL engine generally due to language-related issues
			else if( ex instanceof CqlException ) {
				serviceErrorCode = Status.BAD_REQUEST.getStatusCode();
				serviceErrorListCode = serviceErrorCode;
				errorSource = ErrorSource.COHORT_SERVICE;
			}
			//will get thrown by Jackson deserialization for various types of 
			//parsing errors.
			else if (ex instanceof MismatchedInputException) {
				serviceErrorCode = Status.BAD_REQUEST.getStatusCode();
				serviceErrorListCode = serviceErrorCode;
				errorSource = ErrorSource.COHORT_SERVICE;
			}
			else if (ex instanceof JsonParseException) {
				serviceErrorCode = Status.BAD_REQUEST.getStatusCode();
				serviceErrorListCode = serviceErrorCode;
				description = "Invalid JSON input";
				errorSource = ErrorSource.COHORT_SERVICE;
			}
			//will get thrown by HAPI FHIR when a requested resource is not found in the target FHIR server
			else if (ex instanceof BaseServerResponseException) { 
				serviceErrorCode = Status.BAD_REQUEST.getStatusCode();
				serviceErrorListCode = serviceErrorCode;
				
				BaseServerResponseException sre = (BaseServerResponseException) ex;
				reason = "Exception while communicating with FHIR";
				errorSource = ErrorSource.FHIR_SERVER;

				if( sre.getResponseBody() != null ) {
					description = sre.getResponseBody(); 
				} else { 
					// Some errors do not have a response body 
					description = sre.getLocalizedMessage();
				}
			}
			//catch everything else and return a 500
			else {
				serviceErrorCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();
				serviceErrorListCode = serviceErrorCode;
				description = ex.getMessage();
				errorSource = ErrorSource.COHORT_SERVICE;
			}
			
			if(reason.isEmpty()) {
				reason = ex.getLocalizedMessage();
			}
			se = new ServiceError(serviceErrorCode, reason);
			se.setDescription(description);
			errorsList.add(se);
			//loop through the exception chain logging the cause of each one
			//since these can contain valuable information about the root problems
			createServiceErrorsForExceptions(ex, serviceErrorCode, errorsList);
			serviceErrorList = serviceErrorList
					.statusCode(serviceErrorListCode)
					.errorSource(errorSource);

			logger.error("HTTP Status: "+serviceErrorList.getStatusCode(), ex);
		}
		catch(Throwable nestedEx) {
			// This should not really occur unless there is a bug in this code.
			// Build a 500 ServiceError with some detail
			se = new ServiceError(
					Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					nestedEx.getLocalizedMessage()
			);
			se.setDescription("Reason: Uncaught nested exception");

			logger.error("HTTP Status: "+se.getCode()+", Nested Exception", nestedEx);
			logger.error("Original Exception", ex);
			serviceErrorList = serviceErrorList
					.statusCode(se.getCode())
					.errorSource(ErrorSource.COHORT_SERVICE);

			errorsList.add(se);
		}
		
		return serviceErrorList;
	}
	
	private void createServiceErrorsForExceptions(Throwable ex, int code, List<ServiceError> errorsList) {
		Throwable cause = ex.getCause();
		int causeCount = 0;
		//prevent infinite loop by only going back so many depths
		//exceptions can link to each other so we will only return maxExceptionCauseDepth
		//number of them through the UI, the full stack will get logged if needed in calling method
		while(cause != null && causeCount < MAX_EXCEPTION_CAUSE_DEPTH) {
			if(cause.getLocalizedMessage() != null && !cause.getLocalizedMessage().trim().isEmpty()) {
				ServiceError error = new ServiceError(code, cause.getLocalizedMessage());
				errorsList.add(error);
			}
			cause = cause.getCause();
			causeCount++;
		}
	}

}
