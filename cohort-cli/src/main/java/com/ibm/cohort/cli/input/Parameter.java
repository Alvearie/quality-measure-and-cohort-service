/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Parameter {
	@JsonProperty("name")
	private String name;

	@JsonProperty("type")
	private String type;

	@JsonProperty("valueset")
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
		if (isEmpty(name) || isEmpty(type)) {
			throw new IllegalArgumentException("Invalid measure parameter file: Must provide \"name\" and \"type\" for each measure parameter.");
		}
		// When dealing with an interval, require subtype, start and end. Otherwise require a valueset
		if (type.equals("interval")) {
			if (isEmpty(subtype) || isEmpty(start) || isEmpty(end)) {
				throw new IllegalArgumentException("Invalid measure parameter file: Must provide \"subtype\", \"start\",  and \"end\" for each measure parameter with type \"interval\".");
			}
		}
		else if (isEmpty(value)) {
			throw new IllegalArgumentException("Invalid measure parameter file: Must provide \"valueset\" for each non-interval parameter.");
		}
	}
}
