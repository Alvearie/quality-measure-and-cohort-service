/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
@Generated
public class PatientMeasureEvaluationGroupStratifier {

	private String code = null;
	private List<PatientMeasureEvaluationGroupStratifierStratum> stratum = new ArrayList<PatientMeasureEvaluationGroupStratifierStratum>();

	/*
	 * What stratifier of the group
	 */
	public PatientMeasureEvaluationGroupStratifier code(String code) {
		this.code = code;
		return this;
	}

	@ApiModelProperty(value = "What stratifier of the group")
	@JsonProperty("code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public PatientMeasureEvaluationGroupStratifier stratum(
			List<PatientMeasureEvaluationGroupStratifierStratum> stratum) {
		this.stratum = stratum;
		return this;
	}

	@ApiModelProperty(value = "")
	@JsonProperty("stratum")
	public List<PatientMeasureEvaluationGroupStratifierStratum> getStratum() {
		return stratum;
	}

	public void setStratum(List<PatientMeasureEvaluationGroupStratifierStratum> stratum) {
		this.stratum = stratum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientMeasureEvaluationGroupStratifier patientMeasureEvaluationGroupStratifier = (PatientMeasureEvaluationGroupStratifier) o;
		return Objects.equals(code, patientMeasureEvaluationGroupStratifier.code)
				&& Objects.equals(stratum, patientMeasureEvaluationGroupStratifier.stratum);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, stratum);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PatientMeasureEvaluationGroupStratifier {\n");

		sb.append("    code: ").append(toIndentedString(code)).append("\n");
		sb.append("    stratum: ").append(toIndentedString(stratum)).append("\n");
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
