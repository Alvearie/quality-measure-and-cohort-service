/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.cohort.cli.ParameterHelper;

public class MeasureConfiguration {
	@JsonProperty("measureId")
	private String measureId;

	@JsonProperty("parameters")
	private List<Parameter> parameters;

	public String getMeasureId() {
		return measureId;
	}
	
	public Map<String, Object> getParameters() {
		return ParameterHelper.parseParameters(parameters);
	}
	
	public void validate() throws IllegalArgumentException {
		if (isEmpty(measureId)) {
			throw new IllegalArgumentException("Invalid measure parameter file: A resource id must be provided for each measure.");
		}
		if (parameters != null) {
			parameters.forEach(Parameter::validate);
		}
	}
}
