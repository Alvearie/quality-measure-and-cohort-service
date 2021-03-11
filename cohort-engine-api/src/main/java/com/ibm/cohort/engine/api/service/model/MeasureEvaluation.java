/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.Map;

import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import io.swagger.annotations.ApiModelProperty;

public class MeasureEvaluation {

	private FhirServerConfig dataServerConfig;
	private FhirServerConfig terminologyServerConfig;
	private String patientId;
	private String measureId;

	private Map<String,Parameter> parameterOverrides;
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

	@ApiModelProperty(required = true)
	public String getMeasureId() {
		return measureId;
	}

	public void setMeasureId(String measureId) {
		this.measureId = measureId;
	}

	@ApiModelProperty(required = false)
	public Map<String, Parameter> getParameterOverrides() {
		return parameterOverrides;
	}

	public void setParameterOverrides(Map<String, Parameter> parameterOverrides) {
		this.parameterOverrides = parameterOverrides;
	}

	@ApiModelProperty(required = false)
	public MeasureEvidenceOptions getEvidenceOptions() {
		return evidenceOptions;
	}

	public void setEvidenceOptions(MeasureEvidenceOptions evidenceOptions) {
		this.evidenceOptions = evidenceOptions;
	}
}
