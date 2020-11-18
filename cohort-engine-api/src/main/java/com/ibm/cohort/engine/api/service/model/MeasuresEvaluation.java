/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class MeasuresEvaluation {

	private String patientsTenantId = null;
	private String patientsServerUrl = null;
	private List<String> patientServerConnectionProperties = new ArrayList<String>();
	private String measureTenantId = null;
	private String measureServerUrl = null;
	private List<String> measureServerConnectionProperties = new ArrayList<String>();
	private Integer resultsValidTil = 120;
	private List<PatientMeasureEvaluations> measureEvaluations = new ArrayList<PatientMeasureEvaluations>();

	/**
	 * Tenant identifier for the tenant these patients are associated with
	 **/
	public MeasuresEvaluation patientsTenantId(String patientsTenantId) {
		this.patientsTenantId = patientsTenantId;
		return this;
	}

	@ApiModelProperty(value = "Tenant identifier for the tenant these patients are associated with")
	@JsonProperty("patientsTenantId")
	public String getPatientsTenantId() {
		return patientsTenantId;
	}

	public void setPatientsTenantId(String patientsTenantId) {
		this.patientsTenantId = patientsTenantId;
	}

	/**
	 * URL specifying the server used to store the patients
	 **/
	public MeasuresEvaluation patientsServerUrl(String patientsServerUrl) {
		this.patientsServerUrl = patientsServerUrl;
		return this;
	}

	@ApiModelProperty(value = "URL specifying the server used to store the patients")
	@JsonProperty("patientsServerUrl")
	public String getPatientsServerUrl() {
		return patientsServerUrl;
	}

	public void setPatientsServerUrl(String patientsServerUrl) {
		this.patientsServerUrl = patientsServerUrl;
	}

	/**
	 * A list of connection property strings to be used for the measure server
	 **/
	public MeasuresEvaluation patientServerConnectionProperties(List<String> patientServerConnectionProperties) {
		this.patientServerConnectionProperties = patientServerConnectionProperties;
		return this;
	}

	@ApiModelProperty(value = "A list of connection property strings to be used for the measure server")
	@JsonProperty("patientServerConnectionProperties")
	public List<String> getPatientServerConnectionProperties() {
		return patientServerConnectionProperties;
	}

	public void setPatientServerConnectionProperties(List<String> patientServerConnectionProperties) {
		this.patientServerConnectionProperties = patientServerConnectionProperties;
	}

	/**
	 * Tenant identifier for the tenant this measure is associated with
	 **/
	public MeasuresEvaluation measureTenantId(String measureTenantId) {
		this.measureTenantId = measureTenantId;
		return this;
	}

	@ApiModelProperty(required = true, value = "Tenant identifier for the tenant this measure is associated with")
	@JsonProperty("measureTenantId")
	@NotNull
	public String getMeasureTenantId() {
		return measureTenantId;
	}

	public void setMeasureTenantId(String measureTenantId) {
		this.measureTenantId = measureTenantId;
	}

	/**
	 * URL specifying the server used to store the measure
	 **/
	public MeasuresEvaluation measureServerUrl(String measureServerUrl) {
		this.measureServerUrl = measureServerUrl;
		return this;
	}

	@ApiModelProperty(required = true, value = "URL specifying the server used to store the measure")
	@JsonProperty("measureServerUrl")
	@NotNull
	public String getMeasureServerUrl() {
		return measureServerUrl;
	}

	public void setMeasureServerUrl(String measureServerUrl) {
		this.measureServerUrl = measureServerUrl;
	}

	/**
	 * A list of connection property strings to be used for the measure server
	 **/
	public MeasuresEvaluation measureServerConnectionProperties(List<String> measureServerConnectionProperties) {
		this.measureServerConnectionProperties = measureServerConnectionProperties;
		return this;
	}

	@ApiModelProperty(value = "A list of connection property strings to be used for the measure server")
	@JsonProperty("measureServerConnectionProperties")
	public List<String> getMeasureServerConnectionProperties() {
		return measureServerConnectionProperties;
	}

	public void setMeasureServerConnectionProperties(List<String> measureServerConnectionProperties) {
		this.measureServerConnectionProperties = measureServerConnectionProperties;
	}

	/**
	 * Number of minutes the job results will be available after the job completes
	 **/
	public MeasuresEvaluation resultsValidTil(Integer resultsValidTil) {
		this.resultsValidTil = resultsValidTil;
		return this;
	}

	@ApiModelProperty(value = "Number of minutes the job results will be available after the job completes")
	@JsonProperty("resultsValidTil")
	public Integer getResultsValidTil() {
		return resultsValidTil;
	}

	public void setResultsValidTil(Integer resultsValidTil) {
		this.resultsValidTil = resultsValidTil;
	}

	/**
	 **/
	public MeasuresEvaluation measureEvaluations(List<PatientMeasureEvaluations> measureEvaluations) {
		this.measureEvaluations = measureEvaluations;
		return this;
	}

	@ApiModelProperty(required = true, value = "")
	@JsonProperty("MeasureEvaluations")
	@NotNull
	public List<PatientMeasureEvaluations> getMeasureEvaluations() {
		return measureEvaluations;
	}

	public void setMeasureEvaluations(List<PatientMeasureEvaluations> measureEvaluations) {
		this.measureEvaluations = measureEvaluations;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MeasuresEvaluation measuresEvaluation = (MeasuresEvaluation) o;
		return Objects.equals(patientsTenantId, measuresEvaluation.patientsTenantId)
				&& Objects.equals(patientsServerUrl, measuresEvaluation.patientsServerUrl)
				&& Objects.equals(patientServerConnectionProperties,
						measuresEvaluation.patientServerConnectionProperties)
				&& Objects.equals(measureTenantId, measuresEvaluation.measureTenantId)
				&& Objects.equals(measureServerUrl, measuresEvaluation.measureServerUrl)
				&& Objects.equals(measureServerConnectionProperties,
						measuresEvaluation.measureServerConnectionProperties)
				&& Objects.equals(resultsValidTil, measuresEvaluation.resultsValidTil)
				&& Objects.equals(measureEvaluations, measuresEvaluation.measureEvaluations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(patientsTenantId, patientsServerUrl, patientServerConnectionProperties, measureTenantId,
				measureServerUrl, measureServerConnectionProperties, resultsValidTil, measureEvaluations);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class MeasuresEvaluation {\n");

		sb.append("    patientsTenantId: ").append(toIndentedString(patientsTenantId)).append("\n");
		sb.append("    patientsServerUrl: ").append(toIndentedString(patientsServerUrl)).append("\n");
		sb.append("    patientServerConnectionProperties: ").append(toIndentedString(patientServerConnectionProperties))
				.append("\n");
		sb.append("    measureTenantId: ").append(toIndentedString(measureTenantId)).append("\n");
		sb.append("    measureServerUrl: ").append(toIndentedString(measureServerUrl)).append("\n");
		sb.append("    measureServerConnectionProperties: ").append(toIndentedString(measureServerConnectionProperties))
				.append("\n");
		sb.append("    resultsValidTil: ").append(toIndentedString(resultsValidTil)).append("\n");
		sb.append("    measureEvaluations: ").append(toIndentedString(measureEvaluations)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
