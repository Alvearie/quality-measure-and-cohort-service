/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class PatientMeasureEvaluationPeriod {

	private java.util.Date start = null;
	private java.util.Date end = null;

	/**
	 * Starting time with inclusive boundary
	 **/
	public PatientMeasureEvaluationPeriod start(java.util.Date start) {
		this.start = start;
		return this;
	}

	@ApiModelProperty(value = "Starting time with inclusive boundary")
	@JsonProperty("start")
	public java.util.Date getStart() {
		return start;
	}

	public void setStart(java.util.Date start) {
		this.start = start;
	}

	/**
	 * End time with inclusive boundary, if not ongoing
	 **/
	public PatientMeasureEvaluationPeriod end(java.util.Date end) {
		this.end = end;
		return this;
	}

	@ApiModelProperty(value = "End time with inclusive boundary, if not ongoing")
	@JsonProperty("end")
	public java.util.Date getEnd() {
		return end;
	}

	public void setEnd(java.util.Date end) {
		this.end = end;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientMeasureEvaluationPeriod patientMeasureEvaluationPeriod = (PatientMeasureEvaluationPeriod) o;
		return Objects.equals(start, patientMeasureEvaluationPeriod.start)
				&& Objects.equals(end, patientMeasureEvaluationPeriod.end);
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PatientMeasureEvaluationPeriod {\n");

		sb.append("    start: ").append(toIndentedString(start)).append("\n");
		sb.append("    end: ").append(toIndentedString(end)).append("\n");
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
