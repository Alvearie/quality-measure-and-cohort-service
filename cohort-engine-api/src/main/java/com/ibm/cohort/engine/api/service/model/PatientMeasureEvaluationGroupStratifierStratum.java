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

public class PatientMeasureEvaluationGroupStratifierStratum {

	private String value = null;
	private List<PatientMeasureEvaluationGroupStratifierStratumComponent> component = new ArrayList<PatientMeasureEvaluationGroupStratifierStratumComponent>();
	private List<PatientMeasureEvaluationPopulation> population = new ArrayList<PatientMeasureEvaluationPopulation>();
	private PatientMeasureEvaluationQuantity measureScore = null;

	/**
	 * The stratum value, e.g. male
	 **/
	public PatientMeasureEvaluationGroupStratifierStratum value(String value) {
		this.value = value;
		return this;
	}

	@ApiModelProperty(value = "The stratum value, e.g. male")
	@JsonProperty("value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Stratifier component values
	 **/
	public PatientMeasureEvaluationGroupStratifierStratum component(
			List<PatientMeasureEvaluationGroupStratifierStratumComponent> component) {
		this.component = component;
		return this;
	}

	@ApiModelProperty(value = "Stratifier component values")
	@JsonProperty("component")
	public List<PatientMeasureEvaluationGroupStratifierStratumComponent> getComponent() {
		return component;
	}

	public void setComponent(List<PatientMeasureEvaluationGroupStratifierStratumComponent> component) {
		this.component = component;
	}

	/**
	 * Population results in this stratum
	 **/
	public PatientMeasureEvaluationGroupStratifierStratum population(
			List<PatientMeasureEvaluationPopulation> population) {
		this.population = population;
		return this;
	}

	@ApiModelProperty(value = "Population results in this stratum")
	@JsonProperty("population")
	public List<PatientMeasureEvaluationPopulation> getPopulation() {
		return population;
	}

	public void setPopulation(List<PatientMeasureEvaluationPopulation> population) {
		this.population = population;
	}

	/**
	 **/
	public PatientMeasureEvaluationGroupStratifierStratum measureScore(PatientMeasureEvaluationQuantity measureScore) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientMeasureEvaluationGroupStratifierStratum patientMeasureEvaluationGroupStratifierStratum = (PatientMeasureEvaluationGroupStratifierStratum) o;
		return Objects.equals(value, patientMeasureEvaluationGroupStratifierStratum.value)
				&& Objects.equals(component, patientMeasureEvaluationGroupStratifierStratum.component)
				&& Objects.equals(population, patientMeasureEvaluationGroupStratifierStratum.population)
				&& Objects.equals(measureScore, patientMeasureEvaluationGroupStratifierStratum.measureScore);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, component, population, measureScore);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PatientMeasureEvaluationGroupStratifierStratum {\n");

		sb.append("    value: ").append(toIndentedString(value)).append("\n");
		sb.append("    component: ").append(toIndentedString(component)).append("\n");
		sb.append("    population: ").append(toIndentedString(population)).append("\n");
		sb.append("    measureScore: ").append(toIndentedString(measureScore)).append("\n");
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
