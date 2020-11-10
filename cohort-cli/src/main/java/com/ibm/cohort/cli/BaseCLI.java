/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.FhirServerConfig;

public class BaseCLI {
	
	protected FhirServerConfig dataServerConfig;
	protected FhirServerConfig terminologyServerConfig;
	protected FhirServerConfig measureServerConfig;

	protected void readConnectionConfiguration(ConnectionArguments arguments) throws Exception {
		ObjectMapper om = new ObjectMapper();
		
		dataServerConfig = om.readValue(arguments.dataServerConfigFile, FhirServerConfig.class);

		if (arguments.terminologyServerConfigFile != null) {
			terminologyServerConfig = om.readValue(arguments.terminologyServerConfigFile, FhirServerConfig.class);
		} else {
			terminologyServerConfig = dataServerConfig;
		}

		if (arguments.measureServerConfigFile != null) {
			measureServerConfig = om.readValue(arguments.measureServerConfigFile, FhirServerConfig.class);
		} else { 
			measureServerConfig = dataServerConfig;
		}
	}
}
