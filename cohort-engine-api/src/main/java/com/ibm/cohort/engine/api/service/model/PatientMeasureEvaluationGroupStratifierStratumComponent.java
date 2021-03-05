/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
@Generated
public class PatientMeasureEvaluationGroupStratifierStratumComponent {

	private String code = null;
	private String value = null;

	/*
	 * What stratifier component of the group
	 */
	public PatientMeasureEvaluationGroupStratifierStratumComponent code(String code) {
		this.code = code;
		return this;
	}

	@ApiModelProperty(value = "What stratifier component of the group")
	@JsonProperty("code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/*
	 * The stratum component value, e.g. male
	 */
	public PatientMeasureEvaluationGroupStratifierStratumComponent value(String value) {
		this.value = value;
		return this;
	}

	@ApiModelProperty(value = "The stratum component value, e.g. male")
	@JsonProperty("value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientMeasureEvaluationGroupStratifierStratumComponent patientMeasureEvaluationGroupStratifierStratumComponent = (PatientMeasureEvaluationGroupStratifierStratumComponent) o;
		return Objects.equals(code, patientMeasureEvaluationGroupStratifierStratumComponent.code)
				&& Objects.equals(value, patientMeasureEvaluationGroupStratifierStratumComponent.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PatientMeasureEvaluationGroupStratifierStratumComponent {\n");

		sb.append("    code: ").append(toIndentedString(code)).append("\n");
		sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
