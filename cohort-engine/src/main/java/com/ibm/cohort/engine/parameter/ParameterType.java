/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.parameter;

public class ParameterType {
	private ParameterType() {}
	
	public static final String INTEGER = "integer";
	public static final String DECIMAL = "decimal";
	public static final String BOOLEAN = "boolean";
	public static final String STRING = "string";
	public static final String DATETIME = "datetime";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String QUANTITY = "quantity";
	public static final String RATIO = "ratio";
	public static final String CODE = "code";
	public static final String CONCEPT = "concept";
	public static final String INTERVAL = "interval";
	public static final String SIMPLE = "simple";
}
