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
public class PatientMeasureEvaluationGroup {

	private String code = null;
	private PatientMeasureEvaluationPopulation population = null;
	private PatientMeasureEvaluationQuantity measureScore = null;
	private List<PatientMeasureEvaluationGroupStratifier> stratifier = new ArrayList<PatientMeasureEvaluationGroupStratifier>();

	/*
	 * Meaning of the group
	 */
	public PatientMeasureEvaluationGroup code(String code) {
		this.code = code;
		return this;
	}

	@ApiModelProperty(value = "Meaning of the group")
	@JsonProperty("code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public PatientMeasureEvaluationGroup population(PatientMeasureEvaluationPopulation population) {
		this.population = population;
		return this;
	}

	@ApiModelProperty(value = "")
	@JsonProperty("population")
	public PatientMeasureEvaluationPopulation getPopulation() {
		return population;
	}

	public void setPopulation(PatientMeasureEvaluationPopulation population) {
		this.population = population;
	}

	public PatientMeasureEvaluationGroup measureScore(PatientMeasureEvaluationQuantity measureScore) {
		this.measureScore = measureScore;
		return this;
	}

	@ApiModelProperty(value = "")
	@JsonProperty("measureScore")
	public PatientMeasureEvaluationQuantity getMeasureScore() {
		return measureScore;
	}

	public void setMeasureScore(PatientMeasureEvaluationQuantity measureScore) {
		this.measureScore = measureScore;
	}

	public PatientMeasureEvaluationGroup stratifier(List<PatientMeasureEvaluationGroupStratifier> stratifier) {
		this.stratifier = stratifier;
		return this;
	}

	@ApiModelProperty(value = "")
	@JsonProperty("stratifier")
	public List<PatientMeasureEvaluationGroupStratifier> getStratifier() {
		return stratifier;
	}

	public void setStratifier(List<PatientMeasureEvaluationGroupStratifier> stratifier) {
		this.stratifier = stratifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientMeasureEvaluationGroup patientMeasureEvaluationGroup = (PatientMeasureEvaluationGroup) o;
		return Objects.equals(code, patientMeasureEvaluationGroup.code)
				&& Objects.equals(population, patientMeasureEvaluationGroup.population)
				&& Objects.equals(measureScore, patientMeasureEvaluationGroup.measureScore)
				&& Objects.equals(stratifier, patientMeasureEvaluationGroup.stratifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, population, measureScore, stratifier);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PatientMeasureEvaluationGroup {\n");

		sb.append("    code: ").append(toIndentedString(code)).append("\n");
		sb.append("    population: ").append(toIndentedString(population)).append("\n");
		sb.append("    measureScore: ").append(toIndentedString(measureScore)).append("\n");
		sb.append("    stratifier: ").append(toIndentedString(stratifier)).append("\n");
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
