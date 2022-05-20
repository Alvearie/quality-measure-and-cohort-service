/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.evidence;

public class MeasureEvidenceOptions {
	private boolean includeEvaluatedResources = false;
	private DefineReturnOptions defineReturnOption = DefineReturnOptions.NONE;
	
	public enum DefineReturnOptions {
		ALL,
		BOOLEAN,
		NONE
	}
	
	public MeasureEvidenceOptions() {}
	
	public MeasureEvidenceOptions(boolean includeEvaluatedResources, DefineReturnOptions defineReturnOption) {
		this.includeEvaluatedResources = includeEvaluatedResources;
		this.defineReturnOption = defineReturnOption;
	}
	
	public boolean isIncludeEvaluatedResources() {
		return includeEvaluatedResources;
	}

	public void setIncludeEvaluatedResources(boolean includeEvaluatedResources) {
		this.includeEvaluatedResources = includeEvaluatedResources;
	}
	
	public DefineReturnOptions getDefineReturnOption() {
		return defineReturnOption;
	}

	public void setDefineReturnOption(DefineReturnOptions defineReturnOption) {
		this.defineReturnOption = defineReturnOption;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defineReturnOption == null) ? 0 : defineReturnOption.hashCode());
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
		if (defineReturnOption != other.defineReturnOption)
			return false;
		if (includeEvaluatedResources != other.includeEvaluatedResources)
			return false;
		return true;
	}
}
