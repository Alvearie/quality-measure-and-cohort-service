/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Simple POJO wrapper for the fields in an identifier that are important to the 
 * measure evaluation system. This is preferred over the FHIR object because
 * it can be serialized easily via Jackson where the HAPI FHIR model objects
 * create infinite loops that crater serialization.
 */
public class Identifier {
	private String system;
	private String value;
	
	public Identifier() { } 
	public Identifier( String system, String value ) { 
		setSystem(system);
		setValue(value);
	}
	
	public String getSystem() {
		return system;
	}
	public Identifier setSystem(String system) {
		this.system = system;
		return this;
	}
	public String getValue() {
		return value;
	}
	public Identifier setValue(String value) {
		this.value = value;
		return this;
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}
}
