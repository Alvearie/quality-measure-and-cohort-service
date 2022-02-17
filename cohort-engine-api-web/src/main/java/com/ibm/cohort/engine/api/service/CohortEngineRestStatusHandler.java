/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.api.service.model.EnhancedHealthCheckInput;
import com.ibm.cohort.engine.api.service.model.EnhancedHealthCheckResults;
import com.ibm.cohort.engine.api.service.model.FhirServerConnectionStatusInfo;
import com.ibm.cohort.engine.api.service.model.FhirServerConnectionStatusInfo.FhirConnectionStatus;
import com.ibm.cohort.engine.api.service.model.FhirServerConnectionStatusInfo.FhirServerConfigType;
import com.ibm.cohort.engine.api.service.model.ServiceErrorList;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.watson.common.service.base.ServiceBaseConstants;
import com.ibm.watson.common.service.base.ServiceBaseUtility;
import com.ibm.watson.common.service.base.ServiceStatusHandler;
import com.ibm.watson.service.base.model.ServiceStatus;
import com.ibm.watson.service.base.model.ServiceStatus.ServiceState;
import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Path(CohortEngineRestConstants.SERVICE_MAJOR_VERSION+"/status")
@Api(value = "Status")
@SwaggerDefinition(tags={@Tag(name = "Status", description = "Get the status of this service")})
public class CohortEngineRestStatusHandler extends ServiceStatusHandler {
	private static final Logger logger = LoggerFactory.getLogger(CohortEngineRestStatusHandler.class.getName());
	private static final String HEALTH_CHECK_ENHANCED_API_NOTES = "Checks the status of the cohorting service and any downstream services used by the cohorting service";

	public static final String EXAMPLE_HEALTH_CHECK_DATA_SERVER_CONFIG_JSON = "A configuration file containing information needed to connect to the FHIR server. "
			+ "See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/fhir-server-config.md for more details. \n" +
			"<p>Example Contents: \n <pre>{\n" +
			"    \"dataServerConfig\": {\n" +
			"        \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",\n" +
			"        \"endpoint\": \"ENDPOINT\",\n" +
			"        \"user\": \"USER\",\n" +
			"        \"password\": \"PASSWORD\",\n" +
			"        \"logInfo\": [\n" +
			"            \"REQUEST_SUMMARY\",\n" +
			"            \"RESPONSE_SUMMARY\"\n" +
			"        ],\n" +
			"        \"tenantId\": \"default\"\n" +
			"    },\n" +
			"    \"terminologyServerConfig\": {\n" +
			"        \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",\n" +
			"        \"endpoint\": \"ENDPOINT\",\n" +
			"        \"user\": \"USER\",\n" +
			"        \"password\": \"PASSWORD\",\n" +
			"        \"logInfo\": [\n" +
			"            \"REQUEST_SUMMARY\",\n" +
			"            \"RESPONSE_SUMMARY\"\n" +
			"        ],\n" +
			"        \"tenantId\": \"default\"\n" +
			"    }\n" +
			"}</pre></p>";
	
	public static String GET_HEALTH_CHECK_ENCHANCED = "getHealthCheckEnhanced";
	
	
	/**
	 * Example to show how to modify the service status text.
	 * This method should include code that verifies the service
	 * is running properly.
	 */
	@Override
	public ServiceStatus adjustServiceStatus(ServiceStatus status) {

		status.setServiceState(ServiceState.OK);
		//status.setStateDetails("Running normally");  // No need to set details when state is ok

		return status;
	}
	
	@POST
	@Path("health_check_enhanced")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Get the status of the cohorting service and dependent downstream services", notes = CohortEngineRestStatusHandler.HEALTH_CHECK_ENHANCED_API_NOTES, response = EnhancedHealthCheckResults.class, nickname = "health_check_enhanced")
	@ApiImplicitParams({
		@ApiImplicitParam(name=CohortEngineRestHandler.FHIR_DATA_SERVER_CONFIG_PART, value=CohortEngineRestStatusHandler.EXAMPLE_HEALTH_CHECK_DATA_SERVER_CONFIG_JSON, dataTypeClass = EnhancedHealthCheckInput.class, required=true, paramType="form", type="file"),
	})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful Operation", response = EnhancedHealthCheckResults.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ServiceErrorList.class),
			@ApiResponse(code = 500, message = "Server Error", response = ServiceErrorList.class) })
	public Response getHealthCheckEnhanced(
			@ApiParam(value = ServiceBaseConstants.MINOR_VERSION_DESCRIPTION, required = true, defaultValue = ServiceBuildConstants.DATE) @QueryParam(CohortEngineRestHandler.VERSION) String version,
			@ApiParam(hidden = true, type="file", required=true) IMultipartBody multipartBody)
			{

		final String methodName = CohortEngineRestStatusHandler.GET_HEALTH_CHECK_ENCHANCED;
		
		Response response = null;
		
		try {
			// Perform api setup
			Response errorResponse = ServiceBaseUtility.apiSetup(version, logger, methodName);
			if(errorResponse != null) {
				return errorResponse;
			}
			
			//initialize results object
			EnhancedHealthCheckResults results = new EnhancedHealthCheckResults();
			FhirServerConnectionStatusInfo dataServerConnectionResults = new FhirServerConnectionStatusInfo();
			dataServerConnectionResults.setServerConfigType(FhirServerConfigType.dataServerConfig);
			dataServerConnectionResults.setConnectionResults(FhirConnectionStatus.notAttempted);
			
			FhirServerConnectionStatusInfo terminologyServerConnectionResults = new FhirServerConnectionStatusInfo();
			terminologyServerConnectionResults.setServerConfigType(FhirServerConfigType.terminologyServerConfig);
			terminologyServerConnectionResults.setConnectionResults(FhirConnectionStatus.notAttempted);
			
			results.setDataServerConnectionResults(dataServerConnectionResults);
			results.setTerminologyServerConnectionResults(terminologyServerConnectionResults);

			IAttachment dataSourceAttachment = multipartBody.getAttachment(CohortEngineRestHandler.FHIR_DATA_SERVER_CONFIG_PART);
			if( dataSourceAttachment == null ) {
				throw new IllegalArgumentException(String.format("Missing '%s' MIME attachment", CohortEngineRestHandler.FHIR_DATA_SERVER_CONFIG_PART));
			}
			
			// deserialize the request input
			ObjectMapper om = new ObjectMapper();
			EnhancedHealthCheckInput fhirServerConfigs = om.readValue( dataSourceAttachment.getDataHandler().getInputStream(), EnhancedHealthCheckInput.class );
			
			FhirServerConfig dataServerConfig = fhirServerConfigs.getDataServerConfig();
			FhirServerConfig terminologyServerConfig = fhirServerConfigs.getTerminologyServerConfig();
			
			//validate the contents of the dataServerConfig
			CohortEngineRestHandler.validateBean(dataServerConfig);
			
			//validate the contents of the terminologyServerConfig
			if(terminologyServerConfig != null) {
				CohortEngineRestHandler.validateBean(terminologyServerConfig);
			}

			//get the fhir client object used to call to FHIR
			FhirContext ctx = FhirContext.forR4();
			DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(ctx);
			
			//try a simple patient search to validate the connection info
			IGenericClient dataClient = builder.createFhirClient(dataServerConfig);
			try {
				//used count=0 to minimize response size
				dataClient.search().forResource(Patient.class).count(0).execute();
				dataServerConnectionResults.setConnectionResults(FhirConnectionStatus.success);
			} catch (Throwable ex) {
				dataServerConnectionResults.setConnectionResults(FhirConnectionStatus.failure);
				dataServerConnectionResults.setServiceErrorList(new CohortServiceExceptionMapper().toServiceErrorList(ex));
			}

			//if terminology server info is provided,
			//try a simple valueset search to validate the connection info
			if(terminologyServerConfig != null) {
				try {
					IGenericClient terminologyClient = builder.createFhirClient(terminologyServerConfig);
					//used count=0 to minimize response size
					terminologyClient.search().forResource(ValueSet.class).count(0).execute();
					terminologyServerConnectionResults.setConnectionResults(FhirConnectionStatus.success);
				} catch (Throwable ex) {
					terminologyServerConnectionResults.setConnectionResults(FhirConnectionStatus.failure);
					terminologyServerConnectionResults.setServiceErrorList(new CohortServiceExceptionMapper().toServiceErrorList(ex));
				}
			}

			//return the results
			response = Response.ok(results).build();
		} catch (Throwable e) {
			//map any exceptions caught into the proper REST error response objects
			return new CohortServiceExceptionMapper().toResponse(e);
		}finally {
			// Perform api cleanup
			Response errorResponse = ServiceBaseUtility.apiCleanup(logger, methodName);
			if(errorResponse != null) {
				response = errorResponse;
			}
		}
		
		return response;
	}

}
