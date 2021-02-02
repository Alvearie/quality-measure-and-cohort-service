/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.cohort.annotations.Generated;

public class Parameter {
	private static final String INTERVAL_TYPE = "interval";

	private String name;
	private String type;
	private String value;
	private String subtype;
	private String start;
	private String end;
	
	/**
	 * 
	 * @param name Name of parameter in the CQL
	 * @param type CQL type of the parameter
	 * @param value CQL-formatted string value of the parameter
	 */
	public static Parameter create(String name, String type, String value) {
		Parameter p = new Parameter(name, type, value, null, null, null);
		p.validate();
		return p;
	}

	/**
	 * 
	 * @param name Name of parameter in the CQL
	 * @param subtype Type of interval for the parameter
	 * @param start String representation of the start of the interval
	 * @param end String representation of the end of the interval
	 */
	public static Parameter createInterval(String name, String subtype, String start, String end) {
		Parameter p = new Parameter(name, INTERVAL_TYPE, null, subtype, start, end);
		p.validate();
		return p;
	}

	@JsonCreator
	private Parameter(@JsonProperty("name") String name, @JsonProperty("type") String type,
					  @JsonProperty("value") String value, @JsonProperty("subtype") String subtype,
					  @JsonProperty("start") String start, @JsonProperty("end") String end) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.subtype = subtype;
		this.start = start;
		this.end = end;
	}

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
		// When dealing with an interval, require subtype, start and end. Otherwise require a value
		if (type.equals("interval")) {
			if (isEmpty(subtype) || isEmpty(start) || isEmpty(end)) {
				throw new IllegalArgumentException("Invalid measure parameter file: Must provide \"subtype\", \"start\",  and \"end\" for each measure parameter with type \"interval\".");
			}
		}
		else if (isEmpty(value)) {
			throw new IllegalArgumentException("Invalid measure parameter file: Must provide \"value\" for each non-interval parameter.");
		}
	}

	@Override
	@Generated
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Parameter parameter = (Parameter) o;

		if (name != null ? !name.equals(parameter.name) : parameter.name != null) return false;
		if (type != null ? !type.equals(parameter.type) : parameter.type != null) return false;
		if (value != null ? !value.equals(parameter.value) : parameter.value != null) return false;
		if (subtype != null ? !subtype.equals(parameter.subtype) : parameter.subtype != null) return false;
		if (start != null ? !start.equals(parameter.start) : parameter.start != null) return false;
		return end != null ? end.equals(parameter.end) : parameter.end == null;

	}

	@Override
	@Generated
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
		result = 31 * result + (start != null ? start.hashCode() : 0);
		result = 31 * result + (end != null ? end.hashCode() : 0);
		return result;
	}
}
