/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.evidence;

public class MeasureEvidenceOptions {
	private boolean includeEvaluatedResources = false;
	private boolean includeDefineEvaluation = false;
	
	public MeasureEvidenceOptions() {}
	
	public MeasureEvidenceOptions(boolean includeEvaluatedResources, boolean includeDefineEvaluation) {
		this.includeEvaluatedResources = includeEvaluatedResources;
		this.includeDefineEvaluation = includeDefineEvaluation;
	}

	public boolean isIncludeEvaluatedResources() {
		return includeEvaluatedResources;
	}

	public void setIncludeEvaluatedResources(boolean includeEvaluatedResources) {
		this.includeEvaluatedResources = includeEvaluatedResources;
	}

	public boolean isIncludeDefineEvaluation() {
		return includeDefineEvaluation;
	}

	public void setIncludeDefineEvaluation(boolean includeDefineEvaluation) {
		this.includeDefineEvaluation = includeDefineEvaluation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (includeDefineEvaluation ? 1231 : 1237);
		result = prime * result + (includeEvaluatedResources ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeasureEvidenceOptions other = (MeasureEvidenceOptions) obj;
		if (includeDefineEvaluation != other.includeDefineEvaluation)
			return false;
		if (includeEvaluatedResources != other.includeEvaluatedResources)
			return false;
		return true;
	}
}
