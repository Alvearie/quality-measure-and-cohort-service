/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class PatientMeasureEvaluationQuantity {

	private String fillMeIn = null;

	/**
	 * Meaning of the group
	 **/
	public PatientMeasureEvaluationQuantity fillMeIn(String fillMeIn) {
		this.fillMeIn = fillMeIn;
		return this;
	}

	@ApiModelProperty(value = "Meaning of the group")
	@JsonProperty("fillMeIn")
	public String getFillMeIn() {
		return fillMeIn;
	}

	public void setFillMeIn(String fillMeIn) {
		this.fillMeIn = fillMeIn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientMeasureEvaluationQuantity patientMeasureEvaluationQuantity = (PatientMeasureEvaluationQuantity) o;
		return Objects.equals(fillMeIn, patientMeasureEvaluationQuantity.fillMeIn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fillMeIn);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PatientMeasureEvaluationQuantity {\n");

		sb.append("    fillMeIn: ").append(toIndentedString(fillMeIn)).append("\n");
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
