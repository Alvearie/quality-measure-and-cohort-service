/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cos;

import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

public class CosConfiguration {

	@Parameter(names = { "-k", "--cos-api-key" }, description = "COS API key" )
	private String apiKey;

	@Parameter(names = { "-s", "--cos-id" }, description = "COS service instance id" )
	private String serviceInstanceId;

	@Parameter(names = { "-u", "--cos-url" }, description = "COS endpoint url" )
	private String endpointUrl = "https://s3.us-south.cloud-object-storage.appdomain.cloud";

	@Parameter(names = { "-l", "--cos-location" }, description = "COS Location" )
	private String location = "us";

	@DynamicParameter(names = "-C", description = "Dynamic COS configuration")
	private Map<String, String> params = new HashMap<>();

	public String getApiKey() {
		return apiKey;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public String getEndpointUrl() {
		return endpointUrl;
	}

	public String getLocation() {
		return location;
	}

	public Map<String, String> getParams() {
		return params;
	}
}
