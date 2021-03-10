package com.ibm.cohort.engine.api.service.model;

import java.util.Arrays;
import java.util.List;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;


public class ConceptParameter extends Parameter {
	
	private String display;
	private List<CodeParameter> codes;
	
	public ConceptParameter() {
		setType(ParameterType.CONCEPT);
	}
	
	public ConceptParameter(String name, String display, CodeParameter ...codes) {
		this();
		setName(name);
		setDisplay(display);
		if( codes != null ) {
			setCodes( Arrays.asList(codes) );
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
