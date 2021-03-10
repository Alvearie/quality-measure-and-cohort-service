package com.ibm.cohort.engine.api.service.model;

import org.opencds.cqf.cql.engine.runtime.Code;

public class CodeParameter extends Parameter {
	private String system;
	private String value;
	private String display;
	private String version;
	
	public CodeParameter() {
		setType(ParameterType.CODE);
	}
	
	public CodeParameter(String name, String system, String value, String display, String version) {
		this();
		setName(name);
		setSystem(system);
		setValue(value);
		setDisplay(display);
		setVersion(version);
	}
	
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
