/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeasureParameterInput {
	@JsonProperty("measureParameters")
	private List<MeasureIdWithParameters> measureParameterInputs;

	public List<MeasureIdWithParameters> getMeasureParameterInputs() {
		return measureParameterInputs;
	}
	
	public void validate() {
		if (measureParameterInputs == null || measureParameterInputs.isEmpty()) {
			throw new IllegalArgumentException("Invalid measure parameter file: JSON object must contain information for one or more measures");
		}
		measureParameterInputs.forEach(MeasureIdWithParameters::validate);
	}
}


