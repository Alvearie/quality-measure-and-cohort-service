/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class MeasureParameterInfoList {

	private List<MeasureParameterInfo> parameterInfoList = new ArrayList<MeasureParameterInfo>();
	/**
	 **/
	public MeasureParameterInfoList measureParameterInfoList(List<MeasureParameterInfo> parameterInfoList) {
		this.parameterInfoList = parameterInfoList;
		return this;
	}

	@ApiModelProperty(required = true, value = "A list of parameter information objects for libraried referenced by the input measure")
	@JsonProperty("MeasureParameterInfoList")
	@NotNull
	public List<MeasureParameterInfo> getParameterInfoList() {
		return parameterInfoList;
	}

	public void setParameterInfoList(List<MeasureParameterInfo> parameterInfoList) {
		this.parameterInfoList = parameterInfoList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MeasureParameterInfoList measureParameterInfoList = (MeasureParameterInfoList) o;
		return Objects.equals(measureParameterInfoList, measureParameterInfoList.parameterInfoList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parameterInfoList);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class MeasureParameterInfoList {\n");
		sb.append("    parameterInfoList: ").append(toIndentedString(parameterInfoList)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
