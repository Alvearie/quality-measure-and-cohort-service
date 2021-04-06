/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cos;

public class CosConfiguration {

	private String apiKey;

	private String serviceInstanceId;

	private String endpointUrl;

	private String location;

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

}
