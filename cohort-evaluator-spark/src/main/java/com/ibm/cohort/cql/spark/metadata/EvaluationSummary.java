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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ibm.cohort.cql.spark.errors.EvaluationError;

@JsonPropertyOrder({"applicationId", "startTimeMillis", "endTimeMillis", "runtimeSeconds", "totalContexts", "executionsPerContext", "secondsPerContext", "errorList"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EvaluationSummary {
	private List<EvaluationError> errorList;
	private long startTimeMillis;
	private long endTimeMillis;
	private long runtimeSeconds;
	private long totalContexts;
	private Map<String, Long> executionsPerContext = new HashMap<>();
	private Map<String, Long> secondsPerContext = new HashMap<>();
	private String applicationId;
	
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
	
	@JsonProperty("runtimeSeconds")
	private long getRuntimeSeconds() {
		return (getEndTimeMillis() - getStartTimeMillis()) / 1000;
	}

	public void setRuntimeSeconds(long runtimeSeconds) {
		this.runtimeSeconds = runtimeSeconds;
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

	public Map<String, Long> getSecondsPerContext() {
		return secondsPerContext;
	}

	public void setSecondsPerContext(Map<String, Long> secondsPerContext) {
		this.secondsPerContext = secondsPerContext;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void addContextCount(String contextName, long count) {
		executionsPerContext.put(contextName, count);
	}

	public void addContextRuntime(String contextName, long startTimeMillis, long endTimeMillis) {
		secondsPerContext.put(contextName, (endTimeMillis - startTimeMillis) / 1000);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		EvaluationSummary that = (EvaluationSummary) o;

		return new EqualsBuilder()
				.append(startTimeMillis, that.startTimeMillis)
				.append(endTimeMillis, that.endTimeMillis)
				.append(runtimeSeconds, that.runtimeSeconds)
				.append(totalContexts, that.totalContexts)
				.append(errorList, that.errorList)
				.append(executionsPerContext, that.executionsPerContext)
				.append(secondsPerContext, that.secondsPerContext)
				.append(applicationId, that.applicationId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(errorList)
				.append(startTimeMillis)
				.append(endTimeMillis)
				.append(runtimeSeconds)
				.append(totalContexts)
				.append(executionsPerContext)
				.append(secondsPerContext)
				.append(applicationId)
				.toHashCode();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("EvaluationSummary{");
		sb.append("errorList=").append(errorList);
		sb.append(", startTimeMillis=").append(startTimeMillis);
		sb.append(", endTimeMillis=").append(endTimeMillis);
		sb.append(", runtimeSeconds=").append(runtimeSeconds);
		sb.append(", totalContexts=").append(totalContexts);
		sb.append(", executionsPerContext=").append(executionsPerContext);
		sb.append(", secondsPerContext=").append(secondsPerContext);
		sb.append(", applicationId='").append(applicationId).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
