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
public class EvaluateMeasureResults {

	private String tenant = null;
	private String identifier = null;
	private String status = null;
	private String type = null;
	private String measure = null;
	private List<String> subject = new ArrayList<String>();
	private java.util.Date date = null;
	private String reporter = null;
	private PatientMeasureEvaluationPeriod period = null;
	private String improvementNotation = null;
	private List<PatientMeasureEvaluationGroup> group = new ArrayList<PatientMeasureEvaluationGroup>();
	private List<String> evaluatedResource = new ArrayList<String>();

	/**
	 * Tenant the measure was evaluated for
	 **/
	public EvaluateMeasureResults tenant(String tenant) {
		this.tenant = tenant;
		return this;
	}

	@ApiModelProperty(value = "Tenant the measure was evaluated for")
	@JsonProperty("tenant")
	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	/**
	 * Additional identifier for the MeasureReport
	 **/
	public EvaluateMeasureResults identifier(String identifier) {
		this.identifier = identifier;
		return this;
	}

	@ApiModelProperty(value = "Additional identifier for the MeasureReport")
	@JsonProperty("identifier")
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * complete | pending | error
	 **/
	public EvaluateMeasureResults status(String status) {
		this.status = status;
		return this;
	}

	@ApiModelProperty(value = "complete | pending | error")
	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * individual | subject-list | summary | data-collection
	 **/
	public EvaluateMeasureResults type(String type) {
		this.type = type;
		return this;
	}

	@ApiModelProperty(value = "individual | subject-list | summary | data-collection")
	@JsonProperty("type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * What measure was calculated (measureId)
	 **/
	public EvaluateMeasureResults measure(String measure) {
		this.measure = measure;
		return this;
	}

	@ApiModelProperty(value = "What measure was calculated (measureId)")
	@JsonProperty("measure")
	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	/**
	 * What individual(s) the report is for (subjectId) (Patient | Practitioner |
	 * PractitionerRole | Location | Device | RelatedPerson | Group
	 **/
	public EvaluateMeasureResults subject(List<String> subject) {
		this.subject = subject;
		return this;
	}

	@ApiModelProperty(value = "What individual(s) the report is for (subjectId) (Patient | Practitioner | PractitionerRole | Location | Device | RelatedPerson | Group")
	@JsonProperty("subject")
	public List<String> getSubject() {
		return subject;
	}

	public void setSubject(List<String> subject) {
		this.subject = subject;
	}

	/**
	 * When the report was generated
	 **/
	public EvaluateMeasureResults date(java.util.Date date) {
		this.date = date;
		return this;
	}

	@ApiModelProperty(value = "When the report was generated")
	@JsonProperty("date")
	public java.util.Date getDate() {
		return date;
	}

	public void setDate(java.util.Date date) {
		this.date = date;
	}

	/**
	 * Who is reporting the data
	 **/
	public EvaluateMeasureResults reporter(String reporter) {
		this.reporter = reporter;
		return this;
	}

	@ApiModelProperty(value = "Who is reporting the data")
	@JsonProperty("reporter")
	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	/**
	 **/
	public EvaluateMeasureResults period(PatientMeasureEvaluationPeriod period) {
		this.period = period;
		return this;
	}

	@ApiModelProperty(value = "")
	@JsonProperty("period")
	public PatientMeasureEvaluationPeriod getPeriod() {
		return period;
	}

	public void setPeriod(PatientMeasureEvaluationPeriod period) {
		this.period = period;
	}

	/**
	 * increase | decrease
	 **/
	public EvaluateMeasureResults improvementNotation(String improvementNotation) {
		this.improvementNotation = improvementNotation;
		return this;
	}

	@ApiModelProperty(value = "increase | decrease")
	@JsonProperty("improvementNotation")
	public String getImprovementNotation() {
		return improvementNotation;
	}

	public void setImprovementNotation(String improvementNotation) {
		this.improvementNotation = improvementNotation;
	}

	/**
	 **/
	public EvaluateMeasureResults group(List<PatientMeasureEvaluationGroup> group) {
		this.group = group;
		return this;
	}

	@ApiModelProperty(value = "")
	@JsonProperty("group")
	public List<PatientMeasureEvaluationGroup> getGroup() {
		return group;
	}

	public void setGroup(List<PatientMeasureEvaluationGroup> group) {
		this.group = group;
	}

	/**
	 **/
	public EvaluateMeasureResults evaluatedResource(List<String> evaluatedResource) {
		this.evaluatedResource = evaluatedResource;
		return this;
	}

	@ApiModelProperty(value = "")
	@JsonProperty("evaluatedResource")
	public List<String> getEvaluatedResource() {
		return evaluatedResource;
	}

	public void setEvaluatedResource(List<String> evaluatedResource) {
		this.evaluatedResource = evaluatedResource;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EvaluateMeasureResults evaluateMeasureResults = (EvaluateMeasureResults) o;
		return Objects.equals(tenant, evaluateMeasureResults.tenant)
				&& Objects.equals(identifier, evaluateMeasureResults.identifier)
				&& Objects.equals(status, evaluateMeasureResults.status)
				&& Objects.equals(type, evaluateMeasureResults.type)
				&& Objects.equals(measure, evaluateMeasureResults.measure)
				&& Objects.equals(subject, evaluateMeasureResults.subject)
				&& Objects.equals(date, evaluateMeasureResults.date)
				&& Objects.equals(reporter, evaluateMeasureResults.reporter)
				&& Objects.equals(period, evaluateMeasureResults.period)
				&& Objects.equals(improvementNotation, evaluateMeasureResults.improvementNotation)
				&& Objects.equals(group, evaluateMeasureResults.group)
				&& Objects.equals(evaluatedResource, evaluateMeasureResults.evaluatedResource);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenant, identifier, status, type, measure, subject, date, reporter, period,
				improvementNotation, group, evaluatedResource);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class EvaluateMeasureResults {\n");

		sb.append("    tenant: ").append(toIndentedString(tenant)).append("\n");
		sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("    measure: ").append(toIndentedString(measure)).append("\n");
		sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
		sb.append("    date: ").append(toIndentedString(date)).append("\n");
		sb.append("    reporter: ").append(toIndentedString(reporter)).append("\n");
		sb.append("    period: ").append(toIndentedString(period)).append("\n");
		sb.append("    improvementNotation: ").append(toIndentedString(improvementNotation)).append("\n");
		sb.append("    group: ").append(toIndentedString(group)).append("\n");
		sb.append("    evaluatedResource: ").append(toIndentedString(evaluatedResource)).append("\n");
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

