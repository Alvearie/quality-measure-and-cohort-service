/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeasureParameters {
	@JsonProperty("measureConfigurations")
	private List<MeasureConfiguration> measureConfigurations;

	public List<MeasureConfiguration> getMeasureConfigurations() {
		return measureConfigurations;
	}
	
	public void validate() {
		if (measureConfigurations == null || measureConfigurations.isEmpty()) {
			throw new IllegalArgumentException("Invalid measure parameter file: JSON object must contain information for one or more measures");
		}
		measureConfigurations.forEach(MeasureConfiguration::validate);
	}
}


