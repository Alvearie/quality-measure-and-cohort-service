/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;

@JsonInclude(Include.NON_NULL)
public class MeasureContext {
	private String measureId;
	@Valid
	private Map<String, Parameter> parameters;
	private Identifier identifier;
	private String version;

	protected MeasureContext() { }
	
	public MeasureContext(String measureId) {
		this(measureId, null, null, null);
	}

	public MeasureContext(String measureId, Map<String, Parameter> parameters) {
		this(measureId, parameters, null, null);
	}

	public MeasureContext(String measureId, Map<String, Parameter> parameters, Identifier identifier) {
		this(measureId, parameters, identifier, null);
	}

	public MeasureContext(String measureId, Map<String, Parameter> parameters, Identifier  identifier, String version) {
		this.measureId = measureId;
		this.parameters = parameters;
		this.identifier = identifier;
		this.version = version;
	}

	public String getMeasureId() {
		return measureId;
	}

	public Map<String, Parameter> getParameters() {
		return parameters;
	}

	public Identifier getIdentifier() {
		return identifier;
	}
	
	public String getVersion() {
		return version;
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
	
	@AssertTrue(message="Either measureID or identifier and version are required, but not both")
	@JsonIgnore
	public boolean isExactlyOneFormOfIdentification() {
		boolean hasResourceId = ! StringUtils.isEmpty(measureId);
		boolean hasIdentifier = identifier != null;
		
		return ( hasResourceId ^ hasIdentifier );
	}
	
	@AssertTrue(message="Version should not be specified without identifier")
	@JsonIgnore
	public boolean isVersionOnlyWithIdentifier() {
		return (identifier != null ) || (identifier == null && StringUtils.isEmpty(version) );
	}
	
}
