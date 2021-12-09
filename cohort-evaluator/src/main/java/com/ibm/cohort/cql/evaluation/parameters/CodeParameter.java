/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation.parameters;

import javax.validation.constraints.NotNull;

import org.opencds.cqf.cql.engine.runtime.Code;

public class CodeParameter extends Parameter {
	private String system;
	@NotNull
	private String value;
	private String display;
	private String version;
	
	public CodeParameter() {
		setType(ParameterType.CODE);
	}
	
	public CodeParameter(String system, String value, String display, String version) {
		this();
		setSystem(system);
		setValue(value);
		setDisplay(display);
		setVersion(version);
	}
	
	public String getSystem() {
		return system;
	}
	public CodeParameter setSystem(String system) {
		this.system = system;
		return this;
	}
	public String getValue() {
		return value;
	}
	public CodeParameter setValue(String value) {
		this.value = value;
		return this;
	}

	public String getDisplay() {
		return display;
	}

	public CodeParameter setDisplay(String display) {
		this.display = display;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public CodeParameter setVersion(String version) {
		this.version = version;
		return this;
	}
	
	@Override
	public Object toCqlType() {
		Code code = new Code();
		code.setSystem(system);
		code.setCode(this.value);
		code.setDisplay(this.display);
		code.setVersion(version);
		
		return code;
	}
	
}
