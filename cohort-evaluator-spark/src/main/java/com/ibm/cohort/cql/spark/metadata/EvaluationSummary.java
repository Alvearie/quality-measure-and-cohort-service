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
import com.ibm.cohort.cql.spark.errors.EvaluationError;

@JsonPropertyOrder({"applicationId", "startTimeMillis", "endTimeMillis", "totalContexts", "executionsPerContext", "errorList"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EvaluationSummary {
	private List<EvaluationError> errorList;
	private long startTimeMillis;
	private long endTimeMillis;
	private long totalContexts;
	private Map<String, Long> executionsPerContext = new HashMap<>();
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

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void addContextCount(String contextName, long count) {
		executionsPerContext.put(contextName, count);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		EvaluationSummary that = (EvaluationSummary) o;

		return new EqualsBuilder()
				.append(startTimeMillis, that.startTimeMillis)
				.append(endTimeMillis, that.endTimeMillis)
				.append(totalContexts, that.totalContexts)
				.append(errorList, that.errorList)
				.append(executionsPerContext, that.executionsPerContext)
				.append(applicationId, that.applicationId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(errorList)
				.append(startTimeMillis)
				.append(endTimeMillis)
				.append(totalContexts)
				.append(executionsPerContext)
				.append(applicationId)
				.toHashCode();
	}
}
