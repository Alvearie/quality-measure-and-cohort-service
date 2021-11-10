/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.terminology;

import java.util.Objects;

import org.opencds.cqf.cql.engine.runtime.Code;


/**
 * This class is a simple wrapper class providing equals and hashCode functions
 * for the org.opencds.cqf.cql.engine.runtime.Code class
 * This is used to enable Set lookups for codes within ValueSets, which will be
 * more performant than iterating through a list of codes
 *
 */
public class HashedCode {
	private Code code = null;

	public HashedCode(Code code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof HashedCode) {
			HashedCode hc = (HashedCode)o;
			//we may get codes passed with with only the code, so compare the other fields only if they are present
			if (code.getSystem() == null || (Objects.equals(hc.getCodeObject().getSystem(), code.getSystem())) &&
				code.getVersion() == null || (Objects.equals(hc.getCodeObject().getVersion(), code.getVersion())) &&
				code.getDisplay() == null || (Objects.equals(hc.getCodeObject().getDisplay(), code.getDisplay())) &&
				code.getCode().equalsIgnoreCase(hc.getCodeObject().getCode())){
				return true;
			}
		}
		return false;
	}
	
	public Code getCodeObject() {
		return code;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//only hash the code value since it is possible to get codes passed with
		//without the other fields
		result = prime * result + ((code.getCode() == null) ? 0 : code.getCode().hashCode());
		return result;
	}
	
	@Override
	public String toString() {
		return code.toString();
	}
}