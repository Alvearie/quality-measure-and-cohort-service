/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ibm.cohort.cql.spark.JobStatus;
import com.ibm.cohort.cql.spark.errors.EvaluationError;

@JsonPropertyOrder({"applicationId", "startTimeMillis", "endTimeMillis", "runtimeMillis", "jobStatus", "totalContexts", "executionsPerContext", "runtimeMillisPerContext", "errorList"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EvaluationSummary {
	private List<EvaluationError> errorList;
	private long startTimeMillis;
	private long endTimeMillis;
	private long runtimeMillis;
	private long totalContexts;
	private Map<String, Long> executionsPerContext = new HashMap<>();
	private Map<String, Long> runtimeMillisPerContext = new HashMap<>();
	private String applicationId;
	private String correlationId;
	private JobStatus jobStatus;

	public EvaluationSummary() {
		
	}

	public List<EvaluationError> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<EvaluationError> errorList) {
		this.errorList = errorList;
	}

	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	public void setStartTimeMillis(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
	}

	public long getEndTimeMillis() {
		return endTimeMillis;
	}

	public void setEndTimeMillis(long endTimeMillis) {
		this.endTimeMillis = endTimeMillis;
	}

	public long getRuntimeMillis() {
		return runtimeMillis;
	}

	public void setRuntimeMillis(long runtimeMillis) {
		this.runtimeMillis = runtimeMillis;
	}

	public long getTotalContexts() {
		return totalContexts;
	}

	public void setTotalContexts(long totalContexts) {
		this.totalContexts = totalContexts;
	}

	public Map<String, Long> getExecutionsPerContext() {
		return executionsPerContext;
	}

	public void setExecutionsPerContext(Map<String, Long> executionsPerContext) {
		this.executionsPerContext = executionsPerContext;
	}

	public Map<String, Long> getRuntimeMillisPerContext() {
		return runtimeMillisPerContext;
	}

	public void setRuntimeMillisPerContext(Map<String, Long> runtimeMillisPerContext) {
		this.runtimeMillisPerContext = runtimeMillisPerContext;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public JobStatus getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(JobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}

	public void addContextCount(String contextName, long count) {
		executionsPerContext.put(contextName, count);
	}

	public void addContextRuntime(String contextName, long runtimeMillis) {
		runtimeMillisPerContext.put(contextName, runtimeMillis);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EvaluationSummary that = (EvaluationSummary) o;

		return new EqualsBuilder()
				.append(startTimeMillis, that.startTimeMillis)
				.append(endTimeMillis, that.endTimeMillis)
				.append(runtimeMillis, that.runtimeMillis)
				.append(totalContexts, that.totalContexts)
				.append(errorList, that.errorList)
				.append(executionsPerContext, that.executionsPerContext)
				.append(runtimeMillisPerContext, that.runtimeMillisPerContext)
				.append(applicationId, that.applicationId)
				.append(correlationId, that.correlationId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(errorList)
				.append(startTimeMillis)
				.append(endTimeMillis)
				.append(runtimeMillis)
				.append(totalContexts)
				.append(executionsPerContext)
				.append(runtimeMillisPerContext)
				.append(applicationId)
				.append(correlationId)
				.toHashCode();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("EvaluationSummary{");
		sb.append("errorList=").append(errorList);
		sb.append(", startTimeMillis=").append(startTimeMillis);
		sb.append(", endTimeMillis=").append(endTimeMillis);
		sb.append(", runtimeMillis=").append(runtimeMillis);
		sb.append(", totalContexts=").append(totalContexts);
		sb.append(", executionsPerContext=").append(executionsPerContext);
		sb.append(", runtimeMillisPerContext=").append(runtimeMillisPerContext);
		sb.append(", applicationId='").append(applicationId).append('\'');
		sb.append(", correlationId='").append(correlationId).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
