/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

//This annotation tells jacoco to skip code coverage on these generated placeholder classes
//so the build won't fail
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@interface Generated{}

/*@XmlRootElement uncomment if you want to allow returning xml*/
@Generated
public class EvaluateMeasuresStatus {

	private String jobId = null;
	private java.util.Date jobStartTime = null;
	private java.util.Date jobFinishTime = null;
	private String jobProgress = null;
	private String jobStatus = null;

	/*
	 * Measure evaluation job identifier
	 */
	public EvaluateMeasuresStatus jobId(String jobId) {
		this.jobId = jobId;
		return this;
	}

	@ApiModelProperty(value = "Measure evaluation job identifier")
	@JsonProperty("jobId")
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/*
	 * The time the measure evaluation job was started
	 */
	public EvaluateMeasuresStatus jobStartTime(java.util.Date jobStartTime) {
		this.jobStartTime = jobStartTime;
		return this;
	}

	@ApiModelProperty(value = "The time the measure evaluation job was started")
	@JsonProperty("jobStartTime")
	public java.util.Date getJobStartTime() {
		return jobStartTime;
	}

	public void setJobStartTime(java.util.Date jobStartTime) {
		this.jobStartTime = jobStartTime;
	}

	/*
	 * The time the measure evaluation job finished
	 */
	public EvaluateMeasuresStatus jobFinishTime(java.util.Date jobFinishTime) {
		this.jobFinishTime = jobFinishTime;
		return this;
	}

	@ApiModelProperty(value = "The time the measure evaluation job finished")
	@JsonProperty("jobFinishTime")
	public java.util.Date getJobFinishTime() {
		return jobFinishTime;
	}

	public void setJobFinishTime(java.util.Date jobFinishTime) {
		this.jobFinishTime = jobFinishTime;
	}

	/*
	 * Percentage of the measure evaluation job that is completed
	 */
	public EvaluateMeasuresStatus jobProgress(String jobProgress) {
		this.jobProgress = jobProgress;
		return this;
	}

	@ApiModelProperty(value = "Percentage of the measure evaluation job that is completed")
	@JsonProperty("jobProgress")
	public String getJobProgress() {
		return jobProgress;
	}

	public void setJobProgress(String jobProgress) {
		this.jobProgress = jobProgress;
	}

	/*
	 * Status of the measure evaluation job (eg. running, completed, error etc.)
	 */
	public EvaluateMeasuresStatus jobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
		return this;
	}

	@ApiModelProperty(value = "Status of the measure evaluation job (eg. running, completed, error etc.)")
	@JsonProperty("jobStatus")
	public String getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EvaluateMeasuresStatus evaluateMeasuresStatus = (EvaluateMeasuresStatus) o;
		return Objects.equals(jobId, evaluateMeasuresStatus.jobId)
				&& Objects.equals(jobStartTime, evaluateMeasuresStatus.jobStartTime)
				&& Objects.equals(jobFinishTime, evaluateMeasuresStatus.jobFinishTime)
				&& Objects.equals(jobProgress, evaluateMeasuresStatus.jobProgress)
				&& Objects.equals(jobStatus, evaluateMeasuresStatus.jobStatus);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jobId, jobStartTime, jobFinishTime, jobProgress, jobStatus);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class EvaluateMeasuresStatus {\n");

		sb.append("    jobId: ").append(toIndentedString(jobId)).append("\n");
		sb.append("    jobStartTime: ").append(toIndentedString(jobStartTime)).append("\n");
		sb.append("    jobFinishTime: ").append(toIndentedString(jobFinishTime)).append("\n");
		sb.append("    jobProgress: ").append(toIndentedString(jobProgress)).append("\n");
		sb.append("    jobStatus: ").append(toIndentedString(jobStatus)).append("\n");
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
