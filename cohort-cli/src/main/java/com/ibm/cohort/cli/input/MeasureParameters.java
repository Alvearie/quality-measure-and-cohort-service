/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeasureParameters {
	@JsonProperty("measureParameters")
	private List<MeasureIdWithParameters> measureParameters;

	public List<MeasureIdWithParameters> getMeasureParameters() {
		return measureParameters;
	}
	
	public void validate() {
		if (measureParameters == null || measureParameters.isEmpty()) {
			throw new IllegalArgumentException("Invalid measure parameter file: JSON object must contain information for one or more measures");
		}
		measureParameters.forEach(MeasureIdWithParameters::validate);
	}
}


