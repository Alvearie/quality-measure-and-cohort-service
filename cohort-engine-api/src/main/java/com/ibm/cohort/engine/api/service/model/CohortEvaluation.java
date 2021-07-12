/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.engine.api.service.model;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.ibm.cohort.engine.LoggingEnum;
import com.ibm.cohort.engine.parameter.Parameter;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import io.swagger.annotations.ApiModelProperty;

public class CohortEvaluation {

	@NotNull
	@Valid
	private FhirServerConfig dataServerConfig;
	private FhirServerConfig terminologyServerConfig;
	@NotNull
	private String patientIds;
	@NotNull
	@Valid
	private Map<String, Parameter> parameters;
	@NotNull
	private String entrypoint;
	private LoggingEnum loggingLevel;
	@NotNull
	private String defineToRun;

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

	@ApiModelProperty(required = true)
	public String getPatientIds() {
		return patientIds;
	}

	public void setPatientIds(String patientIds) {
		this.patientIds = patientIds;
	}

	@ApiModelProperty(required=true)
	public Map<String, Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Parameter> parameters) {
		this.parameters = parameters;
	}

	@ApiModelProperty(required = true)
	public String getEntrypoint() {
		return entrypoint;
	}

	public void setEntrypoint(String entrypoint) {
		this.entrypoint = entrypoint;
	}

	@ApiModelProperty(required = true)
	public LoggingEnum getLoggingLevel() {
		return loggingLevel;
	}

	public void setLoggingLevel(LoggingEnum loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	@ApiModelProperty(required = true)
	public String getDefineToRun() {
		return defineToRun;
	}

	public void setDefineToRun(String defineToRun) {
		this.defineToRun = defineToRun;
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
