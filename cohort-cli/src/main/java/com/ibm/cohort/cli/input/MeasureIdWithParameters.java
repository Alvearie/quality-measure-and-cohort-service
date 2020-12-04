package com.ibm.cohort.cli.input;

import static com.ibm.cohort.cli.input.InputUtil.isNullOrEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.cohort.cli.ParameterHelper;

public class MeasureIdWithParameters {
	@JsonProperty("measureId")
	private String measureId;

	@JsonProperty("parameters")
	private List<Parameter> parameters;

	public String getMeasureId() {
		return measureId;
	}
	
	// TODO: Actually implement this for real. Don't use the crazy strings
	public Map<String, Object> getParameters() {
		if (parameters == null) {
			return new HashMap<>();
		}
		
		List<String> stringParameters = new ArrayList<>();
		for (Parameter p: parameters) {
			String valueToParse = p.getValue() != null && !p.getValue().isEmpty() ? p.getValue() : p.getSubtype() + "," + p.getStart() + "," + p.getEnd();
			
			stringParameters.add(p.getName() + ":" + p.getType() + ":" + valueToParse);
		}
		return ParameterHelper.parseParameterArguments(stringParameters);
	}
	
	public void validate() throws IllegalArgumentException {
		if (isNullOrEmpty(measureId)) {
			throw new IllegalArgumentException("Invalid measure parameter file: A resource id must be provided for each measure.");
		}
		if (parameters != null) {
			parameters.forEach(Parameter::validate);
		}
	}
}
