package com.ibm.cohort.cli.input;

import static com.ibm.cohort.cli.input.InputUtil.isNullOrEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Parameter {
	@JsonProperty("name")
	private String name;

	@JsonProperty("type")
	private String type;

	@JsonProperty("value")
	private String value;

	@JsonProperty("subtype")
	private String subtype;

	@JsonProperty("start")
	private String start;

	@JsonProperty("end")
	private String end;

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getSubtype() {
		return subtype;
	}

	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}
	
	public void validate() throws IllegalArgumentException {
		// Require name and type
		if (isNullOrEmpty(name) || isNullOrEmpty(type)) {
			throw new IllegalArgumentException("Invalid measure parameter file: Must provide \"name\" and \"type\" for each measure parameter.");
		}
		// When dealing with an interval, require subtype, start and end. Otherwise require a value
		if (type.equals("interval")) {
			if (isNullOrEmpty(subtype) || isNullOrEmpty(start) || isNullOrEmpty(end)) {
				throw new IllegalArgumentException("Invalid measure parameter file: Must provide \"subtype\", \"start\",  and \"end\" for each measure parameter with type \"interval\".");
			}
		}
		else if (isNullOrEmpty(value)) {
			throw new IllegalArgumentException("Invalid measure parameter file: Must provide \"value\" for each non-interval parameter.");
		}
	}
}
