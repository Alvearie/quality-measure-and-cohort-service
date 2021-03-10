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

public class VersionedMeasures {

	private String measureId = null;
	private String measureVersion = null;
	private List<String> measureParameters = new ArrayList<String>();
	private List<String> measureProperties = new ArrayList<String>();
	private Boolean persistResults = false;

	/*
	 * Measure identifier used to designate a specific measure to evaluate
	 */
	public VersionedMeasures measureId(String measureId) {
		this.measureId = measureId;
		return this;
	}

	@ApiModelProperty(required = true, value = "Measure identifier used to designate a specific measure to evaluate")
	@JsonProperty("measureId")
	@NotNull
	public String getMeasureId() {
		return measureId;
	}

	public void setMeasureId(String measureId) {
		this.measureId = measureId;
	}

	/*
	 * The version of the measure to evaluate. If none is provided, the latest
	 * version of the measure is used.
	 */
	public VersionedMeasures measureVersion(String measureVersion) {
		this.measureVersion = measureVersion;
		return this;
	}

	@ApiModelProperty(value = "The version of the measure to evaluate. If none is provided, the latest version of the measure is used.")
	@JsonProperty("measureVersion")
	public String getMeasureVersion() {
		return measureVersion;
	}

	public void setMeasureVersion(String measureVersion) {
		this.measureVersion = measureVersion;
	}

	/*
	 * A list of parameter strings to be passed to the measure
	 */
	public VersionedMeasures measureParameters(List<String> measureParameters) {
		this.measureParameters = measureParameters;
		return this;
	}

	@ApiModelProperty(value = "A list of parameter strings to be passed to the measure")
	@JsonProperty("measureParameters")
	public List<String> getMeasureParameters() {
		return measureParameters;
	}

	public void setMeasureParameters(List<String> measureParameters) {
		this.measureParameters = measureParameters;
	}

	/*
	 * A list of property strings to be passed to the measure
	 */
	public VersionedMeasures measureProperties(List<String> measureProperties) {
		this.measureProperties = measureProperties;
		return this;
	}

	@ApiModelProperty(value = "A list of property strings to be passed to the measure")
	@JsonProperty("measureProperties")
	public List<String> getMeasureProperties() {
		return measureProperties;
	}

	public void setMeasureProperties(List<String> measureProperties) {
		this.measureProperties = measureProperties;
	}

	/*
	 * If true, results will be persisted in the datastore
	 */
	public VersionedMeasures persistResults(Boolean persistResults) {
		this.persistResults = persistResults;
		return this;
	}

	@ApiModelProperty(value = "If true, results will be persisted in the datastore")
	@JsonProperty("persistResults")
	public Boolean isPersistResults() {
		return persistResults;
	}

	public void setPersistResults(Boolean persistResults) {
		this.persistResults = persistResults;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		VersionedMeasures versionedMeasures = (VersionedMeasures) o;
		return Objects.equals(measureId, versionedMeasures.measureId)
				&& Objects.equals(measureVersion, versionedMeasures.measureVersion)
				&& Objects.equals(measureParameters, versionedMeasures.measureParameters)
				&& Objects.equals(measureProperties, versionedMeasures.measureProperties)
				&& Objects.equals(persistResults, versionedMeasures.persistResults);
	}

	@Override
	public int hashCode() {
		return Objects.hash(measureId, measureVersion, measureParameters, measureProperties, persistResults);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class VersionedMeasures {\n");

		sb.append("    measureId: ").append(toIndentedString(measureId)).append("\n");
		sb.append("    measureVersion: ").append(toIndentedString(measureVersion)).append("\n");
		sb.append("    measureParameters: ").append(toIndentedString(measureParameters)).append("\n");
		sb.append("    measureProperties: ").append(toIndentedString(measureProperties)).append("\n");
		sb.append("    persistResults: ").append(toIndentedString(persistResults)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/*
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
