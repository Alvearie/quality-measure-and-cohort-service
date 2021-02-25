/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.cohort.cli.ParameterHelper;

public class MeasureConfiguration {
	@JsonProperty("measureId")
	private String measureId;

	@JsonProperty("parameters")
	private List<Parameter> parameters;

	@JsonProperty("identifier")
	private MeasureIdentifier identifier;

	@JsonProperty("version")
	private String version;

	public String getMeasureId() {
		return measureId;
	}

	public Map<String, Object> getParameters() {
		return ParameterHelper.parseParameters(parameters);
	}

	public MeasureIdentifier getIdentifier() {
		return identifier;
	}

	public String getVersion() {
		return version;
	}

	public void validate() {
		boolean measureIdSpecified = !isEmpty(measureId);
		boolean identifierSpecified = (identifier != null && !isEmpty(identifier.getValue()));
		
		if (measureIdSpecified == identifierSpecified) {
			throw new IllegalArgumentException("Invalid measure parameter file: Exactly one of id or identifier with a value must be provided for each measure.");
		}

		if (identifier !=  null) {
			identifier.validate();
		}

		if (parameters != null) {
			parameters.forEach(Parameter::validate);
		}
	}
}
