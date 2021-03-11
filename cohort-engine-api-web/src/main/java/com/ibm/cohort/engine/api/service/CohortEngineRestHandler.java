/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MeasureReport;
import org.opencds.cqf.r4.builders.IdentifierBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.api.service.model.MeasureEvaluation;
import com.ibm.cohort.engine.api.service.model.Parameter;
import com.ibm.cohort.engine.api.service.model.MeasureParameterInfo;
import com.ibm.cohort.engine.api.service.model.MeasureParameterInfoList;
import com.ibm.cohort.engine.api.service.model.ServiceErrorList;
import com.ibm.cohort.engine.measure.MeasureEvaluator;
import com.ibm.cohort.engine.measure.ZipResourceResolutionProvider;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.IBMFhirServerConfig;
import com.ibm.watson.common.service.base.ServiceBaseConstants;
import com.ibm.watson.common.service.base.ServiceBaseUtility;
import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.BasicAuthDefinition;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Cohort REST service
 *
 *
 */
@Path(CohortEngineRestConstants.SERVICE_MAJOR_VERSION)
@Api(value = "cohort")
//Need this securityDefinition to get the "Authorize" button on the swagger UI to show up
@SwaggerDefinition(securityDefinition = @SecurityDefinition(basicAuthDefinitions = { @BasicAuthDefinition(key = "BasicAuth") }),
tags={@Tag(name = "FHIR Measures", description = "IBM Cohort Engine FHIR Measure Information"),
						@Tag(name = "measures", description = "IBM Cohort Engine Measure Evaluation")})
public class CohortEngineRestHandler {
	private static final Logger logger = LoggerFactory.getLogger(CohortEngineRestHandler.class.getName());
	private static final String FHIR_ENDPOINT_DESC = "The REST endpoint of the FHIR server the measure/libraries are stored in. For example: "
			+ "https://localhost:9443/fhir-server/api/v4";
	private static final String FHIR_TENANT_HEADER_DESC = "IBM FHIR Server uses HTTP headers to control which underlying tenant contains "
			+ "the data being retrieved. The default header name used to identify the tenant can be changed by the user as needed for their "
			+ "execution environment. If no value is provided, the value in the base configuration files (X-FHIR-TENANT-ID) is used.";
	private static final String FHIR_TENANT_ID_DESC = "The id of the tenant used to store the measure/library information in the FHIR server. ";
	private static final String FHIR_DS_HEADER_DESC = "IBM FHIR Server uses HTTP headers to control which underlying datasource contains "
			+ "the data being retrieved. The default header can be changed by the user as needed for their "
			+ "execution environment. If no value is provided, the value in the base configuration files (X-FHIR-DSID) is used.";
	private static final String FHIR_DS_ID_DESC = "The id of the underlying datasource used by the FHIR server to contain the data.";
	private static final String MEASURE_IDENTIFIER_VALUE_DESC = "Used to identify the FHIR measure resource you would like the parameter information "
			+ "for using the Measure.Identifier.Value field.";
	private static final String MEASURE_ID_DESC = "FHIR measure resource id for the measure you would like the parameter information "
			+ "for using the Measure.id field.";
	private static final String MEASURE_IDENTIFIER_SYSTEM_DESC = "The system name used to provide a namespace for the measure identifier values. For "
			+ "example, if using social security numbers for the identifier values, one would use http://hl7.org/fhir/sid/us-ssn as the system value.";
	private static final String MEASURE_VERSION_DESC = " The version of the measure to retrieve as represented by the FHIR resource Measure.version "
			+ "field. If a value is not provided, the underlying code will atempt to resolve the most recent version assuming a "
			+ "<Major>.<Minor>.<Patch> format (ie if versions 1.0.0 and 2.0.0 both exist, the code will return the 2.0.0 version)";
	private static final String MEASURE_API_NOTES = "Retrieves the parameter information for libraries linked to by a measure";

	private static final String DEFAULT_FHIR_URL = "https://fhir-internal.dev:9443/fhir-server/api/v4";

	public static final String DELAY_DEFAULT = "3";
	
	public static final String REQUEST_DATA_PART = "requestData";
	public static final String MEASURE_PART = "measure";

	static {
		// Long and expensive initialization should occur here or
		// delayed and stored in a static member.

		//logger.setLevel(Level.DEBUG); // TODO: Development time only
	}

	public CohortEngineRestHandler() {
		super();
		// Instance initialization here.
		// NOTE: This constructor is called on every REST call!
	}

	@POST
	@Path("/evaluation")
	//@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Evaluates a measure bundle for a single patient", notes = "Valueset resources required for Measure evaluation must be loaded to the FHIR server in advance of an evaluation request.", response = String.class, tags = {
			"measures" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "successful operation", response = MeasureReport.class),
			@ApiResponse(code = 500, message = "evaluation failed", response = Exception.class) })
	public Response evaluateMeasures(@Context HttpServletRequest request,
			@ApiParam(value = ServiceBaseConstants.MINOR_VERSION_DESCRIPTION, required = true, defaultValue = ServiceBuildConstants.DATE) @QueryParam("version") String version,
			@ApiParam(value = "Multipart form request containing measure evaluation metadata (rootPart) and a measure ZIP artifact (measure)") IMultipartBody multipartBody) {		
		
		ResponseBuilder responseBuilder = null;
		
		try {
			IAttachment metadataAttachment = multipartBody.getAttachment(REQUEST_DATA_PART);
			if( metadataAttachment == null ) {
				throw new IllegalArgumentException(String.format("Missing '%s' MIME attachment", REQUEST_DATA_PART));
			}

			IAttachment measureAttachment = multipartBody.getAttachment(MEASURE_PART);
			if( measureAttachment == null ) {
				throw new IllegalArgumentException(String.format("Missing '%s' MIME attachment", MEASURE_PART));
			}
			
			// deserialize the MeasuresEvaluation request
			ObjectMapper om = new ObjectMapper();
			MeasureEvaluation evaluationRequest = om.readValue( metadataAttachment.getDataHandler().getInputStream(), MeasureEvaluation.class );
			
			Map<String,Object> typedParameters = null;
			if( evaluationRequest.getParameterOverrides() != null ) {
				typedParameters = new HashMap<>();
				for( Map.Entry<String, Parameter> entry : evaluationRequest.getParameterOverrides().entrySet() ) {
					typedParameters.put( entry.getKey(), entry.getValue().toCqlType());
				}
			}
			
			FhirClientBuilder clientBuilder = FhirClientBuilderFactory.newInstance().newFhirClientBuilder();
			IGenericClient dataClient = clientBuilder.createFhirClient(evaluationRequest.getDataServerConfig());
			IGenericClient terminologyClient = dataClient;
			if( evaluationRequest.getTerminologyServerConfig() != null ) {
				terminologyClient = clientBuilder.createFhirClient(evaluationRequest.getTerminologyServerConfig());
			}
	
			IParser parser = dataClient.getFhirContext().newJsonParser();
			
			String [] searchPaths = new String[] { "fhirResources", "fhirResources/libraries" };
			ZipResourceResolutionProvider provider = new ZipResourceResolutionProvider(new ZipInputStream( measureAttachment.getDataHandler().getInputStream()), parser, searchPaths);;
			
			MeasureEvaluator evaluator = new MeasureEvaluator(dataClient, terminologyClient);
			evaluator.setLibraryResolutionProvider(provider);
			evaluator.setMeasureResolutionProvider(provider);
			MeasureReport report = evaluator.evaluatePatientMeasure(evaluationRequest.getMeasureId(), evaluationRequest.getPatientId(), typedParameters, evaluationRequest.getEvidenceOptions());
			
			// The default serializer gets into an infinite loop when trying to serialize MeasureReport, so we use the
			// HAPI encoder instead.
			responseBuilder = Response.status(Response.Status.OK).header("Content-Type", "application/json").entity(parser.encodeResourceToString(report));
		} catch( Exception ex ) {
			//responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex);
			throw new RuntimeException(ex);
		}

		return responseBuilder.build();
	}
	
	@GET
	@Path("/fhir/measure/identifier/{measure_identifier_value}/parameters")
	@Produces({ "application/json" })
	@ApiOperation(value = "Get measure parameters", notes = CohortEngineRestHandler.MEASURE_API_NOTES, response = MeasureParameterInfoList.class, authorizations = {@Authorization(value = "BasicAuth")   }, responseContainer = "List", tags = {
			"FHIR Measures" })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful Operation", response = MeasureParameterInfoList.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ServiceErrorList.class),
			@ApiResponse(code = 500, message = "Server Error", response = ServiceErrorList.class) })
	public Response getMeasureParameters(@Context HttpHeaders httpHeaders,
			@ApiParam(value = ServiceBaseConstants.MINOR_VERSION_DESCRIPTION, required = true, defaultValue = ServiceBuildConstants.DATE) @QueryParam("version") String version,
			@ApiParam(value = CohortEngineRestHandler.FHIR_ENDPOINT_DESC, required = true, defaultValue = CohortEngineRestHandler.DEFAULT_FHIR_URL) @QueryParam("fhir_server_rest_endpoint") String fhirEndpoint,
			@ApiParam(value = CohortEngineRestHandler.FHIR_TENANT_ID_DESC, required = true, defaultValue = "default") @QueryParam("fhir_server_tenant_id") String fhirTenantId,
			@ApiParam(value = CohortEngineRestHandler.MEASURE_IDENTIFIER_VALUE_DESC, required = true) @PathParam("measure_identifier_value") String measureIdentifierValue,
			@ApiParam(value = CohortEngineRestHandler.MEASURE_IDENTIFIER_SYSTEM_DESC, required = false) @QueryParam("measure_identifier_system") String measureIdentifierSystem,
			@ApiParam(value = CohortEngineRestHandler.MEASURE_VERSION_DESC, required = false) @QueryParam("measure_version") String measureVersion,
			@ApiParam(value = CohortEngineRestHandler.FHIR_TENANT_HEADER_DESC, required = false, defaultValue = IBMFhirServerConfig.DEFAULT_TENANT_ID_HEADER) @QueryParam("fhir_server_tenant_id_header") String fhirTenantIdHeader,
			@ApiParam(value = CohortEngineRestHandler.FHIR_DS_HEADER_DESC, required = false, defaultValue = IBMFhirServerConfig.DEFAULT_DATASOURCE_ID_HEADER) @QueryParam("fhir_data_source_id_header") String fhirDataSourceIdHeader,
			@ApiParam(value = CohortEngineRestHandler.FHIR_DS_ID_DESC, required = false) @QueryParam("fhir_data_source_id") String fhirDataSourceId)
			{
		final String methodName = "getMeasureParameters";

		try {
			// Perform api setup
			Response errorResponse = ServiceBaseUtility.apiSetup(version, logger, methodName);
			if(errorResponse != null) {
				return errorResponse;
			}
			
			//get the fhir client object used to call to FHIR
			IGenericClient measureClient = FHIRRestUtils.getFHIRClient(fhirEndpoint, fhirTenantIdHeader, fhirTenantId, fhirDataSourceIdHeader, fhirDataSourceId, httpHeaders);
			
			//build the identifier object which is used by the fhir client
			//to find the measure
			Identifier identifier = new IdentifierBuilder()
					.buildValue(measureIdentifierValue)
					.buildSystem(measureIdentifierSystem)
					.build();

			//resolve the measure, and return the parameter info for all the libraries linked to by the measure
			List<MeasureParameterInfo> parameterInfoList = FHIRRestUtils.getParametersForMeasureIdentifier(measureClient, identifier, measureVersion);

			//return the results
			MeasureParameterInfoList status = new MeasureParameterInfoList().measureParameterInfoList(parameterInfoList);
			return Response.ok(status).build();
		} catch (Throwable e) {
			//map any exceptions caught into the proper REST error response objects
			return new CohortServiceExceptionMapper().toResponse(e);
		}finally {
			// Perform api cleanup
			Response errorResponse = ServiceBaseUtility.apiCleanup(logger, methodName);
			if(errorResponse != null) {
				return errorResponse;
			}
		}

	}
	
	@GET
	@Path("/fhir/measure/{measure_id}/parameters")
	@Produces({ "application/json" })
	@ApiOperation(value = "Get measure parameters by id", notes = CohortEngineRestHandler.MEASURE_API_NOTES, response = MeasureParameterInfoList.class, authorizations = {@Authorization(value = "BasicAuth")   }, responseContainer = "List", tags = {
			"FHIR Measures" })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful Operation", response = MeasureParameterInfoList.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ServiceErrorList.class),
			@ApiResponse(code = 500, message = "Server Error", response = ServiceErrorList.class) })
	public Response getMeasureParametersById(@Context HttpHeaders httpHeaders,
			@ApiParam(value = ServiceBaseConstants.MINOR_VERSION_DESCRIPTION, required = true, defaultValue = ServiceBuildConstants.DATE) @QueryParam("version") String version,
			@ApiParam(value = CohortEngineRestHandler.FHIR_ENDPOINT_DESC, required = true, defaultValue = CohortEngineRestHandler.DEFAULT_FHIR_URL) @QueryParam("fhir_server_rest_endpoint") String fhirEndpoint,
			@ApiParam(value = CohortEngineRestHandler.FHIR_TENANT_ID_DESC, required = true, defaultValue = "default") @QueryParam("fhir_server_tenant_id") String fhirTenantId,
			@ApiParam(value = CohortEngineRestHandler.MEASURE_ID_DESC, required = true) @PathParam("measure_id") String measureId,
			@ApiParam(value = CohortEngineRestHandler.FHIR_TENANT_HEADER_DESC, required = false, defaultValue = IBMFhirServerConfig.DEFAULT_TENANT_ID_HEADER) @QueryParam("fhir_server_tenant_id_header") String fhirTenantIdHeader,
			@ApiParam(value = CohortEngineRestHandler.FHIR_DS_HEADER_DESC, required = false, defaultValue = IBMFhirServerConfig.DEFAULT_DATASOURCE_ID_HEADER) @QueryParam("fhir_data_source_id_header") String fhirDataSourceIdHeader,
			@ApiParam(value = CohortEngineRestHandler.FHIR_DS_ID_DESC, required = false) @QueryParam("fhir_data_source_id") String fhirDataSourceId)
			{
		final String methodName = "getMeasureParametersById";

		try {
			// Perform api setup
			Response errorResponse = ServiceBaseUtility.apiSetup(version, logger, methodName);
			if(errorResponse != null) {
				return errorResponse;
			}
			
			//get the fhir client object used to call to FHIR
			IGenericClient measureClient = FHIRRestUtils.getFHIRClient(fhirEndpoint, fhirTenantIdHeader, fhirTenantId, fhirDataSourceIdHeader, fhirDataSourceId, httpHeaders);

			//resolve the measure, and return the parameter info for all the libraries linked to by the measure
			List<MeasureParameterInfo> parameterInfoList = FHIRRestUtils.getParametersForMeasureId(measureClient, measureId);

			//return the results
			MeasureParameterInfoList status = new MeasureParameterInfoList().measureParameterInfoList(parameterInfoList);
			return Response.ok(status).build();
		} catch (Throwable e) {
			//map any exceptions caught into the proper REST error response objects
			return new CohortServiceExceptionMapper().toResponse(e);
		}finally {
			// Perform api cleanup
			Response errorResponse = ServiceBaseUtility.apiCleanup(logger, methodName);
			if(errorResponse != null) {
				return errorResponse;
			}
		}
	}

}

