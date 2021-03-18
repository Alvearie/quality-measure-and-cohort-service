/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.ibm.cohort.engine.measure.MeasureContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class MeasureEvaluation {

	@NotNull
	private FhirServerConfig dataServerConfig;
	private FhirServerConfig terminologyServerConfig;
	@NotNull
	private String patientId;
	@NotNull
	private MeasureContext measureContext;
	private MeasureEvidenceOptions evidenceOptions;

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
	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	@ApiModelProperty(required=true)
	public MeasureContext getMeasureContext() {
		return measureContext;
	}

	public void setMeasureContext(MeasureContext measureContext) {
		this.measureContext = measureContext;
	}
	
	@ApiModelProperty(required = false)
	public MeasureEvidenceOptions getEvidenceOptions() {
		return evidenceOptions;
	}

	public void setEvidenceOptions(MeasureEvidenceOptions evidenceOptions) {
		this.evidenceOptions = evidenceOptions;
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
