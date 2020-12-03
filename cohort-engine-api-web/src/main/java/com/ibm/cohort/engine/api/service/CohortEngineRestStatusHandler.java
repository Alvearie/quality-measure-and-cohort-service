/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import javax.ws.rs.Path;

import com.ibm.watson.common.service.base.ServiceStatusHandler;
import com.ibm.watson.service.base.model.ServiceStatus;
import com.ibm.watson.service.base.model.ServiceStatus.ServiceState;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Path(CohortEngineRestConstants.SERVICE_MAJOR_VERSION+"/status")
@Api(value = "Status")
@SwaggerDefinition(tags={@Tag(name = "Status", description = "Get the status of this service")})
public class CohortEngineRestStatusHandler extends ServiceStatusHandler {

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

}
