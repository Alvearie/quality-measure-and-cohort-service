/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.cohort.annotations.Generated;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Generated
@ApiModel(value="FhirServerConnectionStatusInfo", description="An object containing the results of an attempt to connect to a FHIR server")
public class FhirServerConnectionStatusInfo {

	public static enum FhirServerConfigType {dataServerConfig, terminologyServerConfig};
	public static enum FhirConnectionStatus {success, failure, notAttempted};
	
	private FhirServerConfigType serverConfigType;
	private FhirConnectionStatus connectionResults;
	private ServiceErrorList serviceErrorList;

	@ApiModelProperty(value = "A string describing the type of FHIR server config")
	@JsonProperty("serverConfigType")
	public FhirServerConfigType getServerConfigType() {
		return serverConfigType;
	}

	public void setServerConfigType(FhirServerConfigType serverConfigType) {
		this.serverConfigType = serverConfigType;
	}

	@ApiModelProperty(value = "Result of the connection attempt")
	@JsonProperty("connectionResults")
	public FhirConnectionStatus getConnectionResults() {
		return connectionResults;
	}

	public void setConnectionResults(FhirConnectionStatus connectionResults) {
		this.connectionResults = connectionResults;
	}

	public ServiceErrorList getServiceErrorList() {
		return serviceErrorList;
	}

	public void setServiceErrorList(ServiceErrorList serviceErrorList) {
		this.serviceErrorList = serviceErrorList;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}
}
