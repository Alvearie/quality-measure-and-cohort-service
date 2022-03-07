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
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Generated
@ApiModel
public class EnhancedHealthCheckInput {

	@NotNull
	@Valid
	private FhirServerConfig dataServerConfig;
	private FhirServerConfig terminologyServerConfig;
	
	@ApiModelProperty(required = true)
	public FhirServerConfig getDataServerConfig() {
		return this.dataServerConfig;
	}

	public void setDataServerConfig(FhirServerConfig dataServerConfig) {
		this.dataServerConfig = dataServerConfig;
	}

	@ApiModelProperty(required = false)
	public FhirServerConfig getTerminologyServerConfig() {
		return this.terminologyServerConfig;
	}

	public void setTerminologyServerConfig(FhirServerConfig terminologyServerConfig) {
		this.terminologyServerConfig = terminologyServerConfig;
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