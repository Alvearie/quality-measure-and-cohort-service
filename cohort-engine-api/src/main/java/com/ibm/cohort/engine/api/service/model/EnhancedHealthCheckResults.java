/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 *  SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.ibm.cohort.annotations.Generated;

import io.swagger.annotations.ApiModel;

@Generated
@ApiModel(value="EnhancedHealthCheckResults", description="An object containing the results of an attempt to connect to the FHIR data server and (optional) terminology server")
public class EnhancedHealthCheckResults {

	@NotNull
	@Valid
	private FhirServerConnectionStatusInfo dataServerConnectionResults;
	private FhirServerConnectionStatusInfo terminologyServerConnectionResults;
	
	public FhirServerConnectionStatusInfo getDataServerConnectionResults() {
		return dataServerConnectionResults;
	}

	public void setDataServerConnectionResults(FhirServerConnectionStatusInfo dataServerConnectionResults) {
		this.dataServerConnectionResults = dataServerConnectionResults;
	}

	public FhirServerConnectionStatusInfo getTerminologyServerConnectionResults() {
		return terminologyServerConnectionResults;
	}

	public void setTerminologyServerConnectionResults(FhirServerConnectionStatusInfo terminologyServerConnectionResults) {
		this.terminologyServerConnectionResults = terminologyServerConnectionResults;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}