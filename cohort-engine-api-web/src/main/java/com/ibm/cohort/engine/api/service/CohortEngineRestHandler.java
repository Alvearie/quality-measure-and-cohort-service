/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.ibm.cohort.engine.api.service.model.EvaluateMeasureResults;
import com.ibm.cohort.engine.api.service.model.EvaluateMeasuresStatus;
import com.ibm.cohort.engine.api.service.model.MeasuresEvaluation;
import com.ibm.watson.common.service.base.ServiceBaseConstants;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Cohort REST service
 *
 *
 */
@Path(CohortEngineRestConstants.SERVICE_MAJOR_VERSION)
@Api(value = "cohort")
@SwaggerDefinition(tags={@Tag(name = "measures", description = "IBM Cohort Engine Measure Evaluation")})
public class CohortEngineRestHandler {
	//private static final Logger logger = LoggerFactory.getLogger(CohortEngineRestHandler.class.getName());

	public static final String DELAY_DEFAULT = "3";

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

	@DELETE
	@Path("/evaluation/{jobId}")
	@Produces({ "application/xml", "application/json" })
	@ApiOperation(value = "Deletes a measure evaluation job", notes = "", response = Void.class, tags = { "measures" })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Measure evaluation job successfully deleted", response = Void.class),
			@ApiResponse(code = 404, message = "Measure evaluation job not found", response = Void.class),
			@ApiResponse(code = 500, message = "Measure evaluation job not deleted", response = Void.class) })
	public Response deleteEvaluation(
			@ApiParam(value = ServiceBaseConstants.MINOR_VERSION_DESCRIPTION, required = true, defaultValue = ServiceBuildConstants.DATE) @QueryParam("version") String version,
			@ApiParam(value = "Job identifier for measure evaluation request", required = true) @PathParam("jobId") String jobId) {
		// return delegate.deleteEvaluation(jobId, securityContext);

		ResponseBuilder responseBuilder = Response.ok(jobId);
		return responseBuilder.build();
	}

	@POST
	@Path("/evaluation")
	@Produces({ "application/json" })
	@ApiOperation(value = "Initiates evaluation of measures for given patients", notes = "Asynchronous evaluation of measures for patients", response = String.class, tags = {
			"measures" })
	@ApiResponses(value = { @ApiResponse(code = 202, message = "successful operation", response = String.class),
			@ApiResponse(code = 500, message = "Evaluation job not created", response = Void.class) })
	public Response evaluateMeasures(@Context HttpServletRequest request,
			@ApiParam(value = ServiceBaseConstants.MINOR_VERSION_DESCRIPTION, required = true, defaultValue = ServiceBuildConstants.DATE) @QueryParam("version") String version,
			@ApiParam(value = "patients and the measures to run", required = true) MeasuresEvaluation body) {
		// return delegate.evaluateMeasures(body, securityContext);
		ResponseBuilder responseBuilder = Response.status(Response.Status.ACCEPTED).entity("12345")
				.header("Content-Location", request.getRequestURL() + "/status/12345?version=" + version);

		return responseBuilder.build();
	}

	@GET
	@Path("/evaluation/status/{jobId}/results")
	@Produces({ "application/json" })
	@ApiOperation(value = "Measure evaluation job results", notes = "Retrieves the results of the asynchronous measure evaluation job", response = EvaluateMeasureResults.class, responseContainer = "List", tags = {
			"measures" })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "successful operation", response = EvaluateMeasureResults.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Measure evaluation job not found", response = Void.class),
			@ApiResponse(code = 500, message = "Error getting job results", response = Void.class) })
	public Response getEvaluateMeasuresResults(
			@ApiParam(value = ServiceBaseConstants.MINOR_VERSION_DESCRIPTION, required = true, defaultValue = ServiceBuildConstants.DATE) @QueryParam("version") String version,
			@ApiParam(value = "Job identifier for measure evaluation request", required = true) @PathParam("jobId") String jobId) {
		// return delegate.getEvaluateMeasuresResults(jobId, securityContext);

		ResponseBuilder responseBuilder = Response.ok("test");
		return responseBuilder.build();
	}

	@GET
	@Path("/evaluation/status/{jobId}")
	@Produces({ "application/json" })
	@ApiOperation(value = "Measure evaluation job status", notes = "Retrieves the status of the asynchronous measure evaluation job", response = EvaluateMeasuresStatus.class, responseContainer = "List", tags = {
			"measures" })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "successful operation", response = EvaluateMeasuresStatus.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Measure evaluation job not found", response = Void.class),
			@ApiResponse(code = 500, message = "Error getting job status", response = Void.class) })
	public Response getEvaluateMeasuresStatus(
			@ApiParam(value = ServiceBaseConstants.MINOR_VERSION_DESCRIPTION, required = true, defaultValue = ServiceBuildConstants.DATE) @QueryParam("version") String version,
			@ApiParam(value = "Job identifier for measure evaluation request", required = true) @PathParam("jobId") String jobId) {
		// return delegate.getEvaluateMeasuresStatus(jobId, securityContext);

		Date finish = new Date();
		Date start = new Date(finish.toInstant().toEpochMilli() - 5000);

		EvaluateMeasuresStatus status = new EvaluateMeasuresStatus().jobId(jobId).jobStatus("RUNNING")
				.jobStartTime(start).jobFinishTime(finish);
		return Response.ok(status).build();
	}

}

