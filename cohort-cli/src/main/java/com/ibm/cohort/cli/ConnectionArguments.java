/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import java.io.File;

import com.beust.jcommander.Parameter;

public class ConnectionArguments {
	@Parameter(names = { "-d",
			"--data-server" }, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve data.", required = true)
	File dataServerConfigFile;

	@Parameter(names = { "-t",
			"--terminology-server" }, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve terminology.", required = false)
	File terminologyServerConfigFile;

	@Parameter(names = { "-m",
			"--measure-server" }, description = "Path to configuration data for the FHIR knowledge assets. This will be either a JSON configuration file containing FHIR server connection details or the path to a file containing the FHIR resources of interest.", required = false)
	File measureServerConfigFile;
}
