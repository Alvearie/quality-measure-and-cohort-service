/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.spark.model;

import java.io.Serializable;

public class CodeWithMetadataPOJO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String codeStr;
	private String system;
	private String display;
	
	public CodeWithMetadataPOJO() {
		
	}
	
	public CodeWithMetadataPOJO(String codeStr, String system, String display) {
		this.codeStr = codeStr;
		this.system = system;
		this.display = display;
	}
	
	public String getCodeStr() {
		return codeStr;
	}
	public void setCodeStr(String codeStr) {
		this.codeStr = codeStr;
	}
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
}
