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

public class PatientMeasureEvaluationPopulation {

	private String code = null;
	private Integer count = null;
	private List<String> subjectResults = new ArrayList<String>();

	/**
	 * initial-population | numerator | numerator-exclusion | denominator |
	 * denominator-exclusion | denominator-exception | measure-population |
	 * measure-population-exclusion | measure-observation
	 **/
	public PatientMeasureEvaluationPopulation code(String code) {
		this.code = code;
		return this;
	}

	@ApiModelProperty(value = "initial-population | numerator | numerator-exclusion | denominator | denominator-exclusion | denominator-exception | measure-population | measure-population-exclusion | measure-observation")
	@JsonProperty("code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Size of the population
	 **/
	public PatientMeasureEvaluationPopulation count(Integer count) {
		this.count = count;
		return this;
	}

	@ApiModelProperty(value = "Size of the population")
	@JsonProperty("count")
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * For subject-list reports, the subject id results in this population**
	 **/
	public PatientMeasureEvaluationPopulation subjectResults(List<String> subjectResults) {
		this.subjectResults = subjectResults;
		return this;
	}

	@ApiModelProperty(value = "For subject-list reports, the subject id results in this population**")
	@JsonProperty("subjectResults")
	public List<String> getSubjectResults() {
		return subjectResults;
	}

	public void setSubjectResults(List<String> subjectResults) {
		this.subjectResults = subjectResults;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientMeasureEvaluationPopulation patientMeasureEvaluationPopulation = (PatientMeasureEvaluationPopulation) o;
		return Objects.equals(code, patientMeasureEvaluationPopulation.code)
				&& Objects.equals(count, patientMeasureEvaluationPopulation.count)
				&& Objects.equals(subjectResults, patientMeasureEvaluationPopulation.subjectResults);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, count, subjectResults);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PatientMeasureEvaluationPopulation {\n");

		sb.append("    code: ").append(toIndentedString(code)).append("\n");
		sb.append("    count: ").append(toIndentedString(count)).append("\n");
		sb.append("    subjectResults: ").append(toIndentedString(subjectResults)).append("\n");
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
