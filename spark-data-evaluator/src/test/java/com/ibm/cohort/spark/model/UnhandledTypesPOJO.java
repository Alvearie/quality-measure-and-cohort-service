/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.spark.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnhandledTypesPOJO {
	private String[] arrayField = new String[] { "Hello", "World" };
	private List<Integer> listField = new ArrayList<>();
	private Map<String,Object> mapField = new HashMap<>();
	private AllTypesJava8DatesPOJO beanField = new AllTypesJava8DatesPOJO();
	
	public String[] getArrayField() {
		return arrayField;
	}
	public void setArrayField(String[] arrayField) {
		this.arrayField = arrayField;
	}
	public List<Integer> getListField() {
		return listField;
	}
	public void setListField(List<Integer> listField) {
		this.listField = listField;
	}
	public Map<String, Object> getMapField() {
		return mapField;
	}
	public void setMapField(Map<String, Object> mapField) {
		this.mapField = mapField;
	}
	public AllTypesJava8DatesPOJO getBeanField() {
		return beanField;
	}
	public void setBeanField(AllTypesJava8DatesPOJO beanField) {
		this.beanField = beanField;
	}
}
