/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;


public class ConceptParameter extends Parameter {
	
	private String display;
	
	@NotNull
	@Size(min=1)
	private List<CodeParameter> codes;
	
	public ConceptParameter() {
		setType(ParameterType.CONCEPT);
	}
	
	public ConceptParameter(String display, CodeParameter ...codes) {
		this();
		setDisplay(display);
		if( codes != null ) {
			setCodes( Arrays.asList(codes) );
		}
	}
	
	public ConceptParameter(String display, List<CodeParameter> codes) {
		this();
		setDisplay(display);
		if( codes != null ) {
			setCodes( codes );
		}
	}
	
	public String getDisplay() {
		return this.display;
	}
	
	public ConceptParameter setDisplay(String display) {
		this.display = display;
		return this;
	}
	
	public List<CodeParameter> getCodes() {
		return codes;
	}

	public void setCodes(List<CodeParameter> codes) {
		this.codes = codes;
	}
	
	@Override
	public Object toCqlType() {
		Concept concept = new Concept();
		concept.withDisplay(this.display);
		if( codes != null ) {
			for( CodeParameter param : codes ) {
				concept.withCode( (Code) param.toCqlType() );
			}
		}
		return concept;
	}
}
