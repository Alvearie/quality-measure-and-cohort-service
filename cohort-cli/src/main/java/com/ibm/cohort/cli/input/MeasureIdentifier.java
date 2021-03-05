/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeasureIdentifier {
	@JsonProperty("system")
	private String system;

	@JsonProperty("valueset")
	private String value;

	public String getSystem() {
		return system;
	}

	public String getValue() {
		return value;
	}

	public void validate() {
		if (isEmpty(value)) {
			throw new IllegalArgumentException("Invalid measure parameter file: A measure identifier must have a valueset.");
		}
	}
}