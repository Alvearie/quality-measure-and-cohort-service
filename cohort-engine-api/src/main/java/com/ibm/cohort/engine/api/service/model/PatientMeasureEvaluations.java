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
@Generated
public class PatientMeasureEvaluations {

	private List<String> patientIds = new ArrayList<String>();
	private List<VersionedMeasures> versionedMeasures = new ArrayList<VersionedMeasures>();

	/**
	 * List of patient identifiers used to designate a specific patient to evaluate
	 * one or more measures against
	 **/
	public PatientMeasureEvaluations patientIds(List<String> patientIds) {
		this.patientIds = patientIds;
		return this;
	}

	@ApiModelProperty(value = "List of patient identifiers used to designate a specific patient to evaluate one or more measures against")
	@JsonProperty("patientIds")
	public List<String> getPatientIds() {
		return patientIds;
	}

	public void setPatientIds(List<String> patientIds) {
		this.patientIds = patientIds;
	}

	/**
	 **/
	public PatientMeasureEvaluations versionedMeasures(List<VersionedMeasures> versionedMeasures) {
		this.versionedMeasures = versionedMeasures;
		return this;
	}

	@ApiModelProperty(required = true, value = "")
	@JsonProperty("versionedMeasures")
	@NotNull
	public List<VersionedMeasures> getVersionedMeasures() {
		return versionedMeasures;
	}

	public void setVersionedMeasures(List<VersionedMeasures> versionedMeasures) {
		this.versionedMeasures = versionedMeasures;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientMeasureEvaluations patientMeasureEvaluations = (PatientMeasureEvaluations) o;
		return Objects.equals(patientIds, patientMeasureEvaluations.patientIds)
				&& Objects.equals(versionedMeasures, patientMeasureEvaluations.versionedMeasures);
	}

	@Override
	public int hashCode() {
		return Objects.hash(patientIds, versionedMeasures);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PatientMeasureEvaluations {\n");

		sb.append("    patientIds: ").append(toIndentedString(patientIds)).append("\n");
		sb.append("    versionedMeasures: ").append(toIndentedString(versionedMeasures)).append("\n");
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
