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

import com.ibm.cohort.annotations.Generated;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Generated
@ApiModel(value="MeasureParameterInfoList", description="An object containing a list of parameter information objects for libraries referenced by the input measure")
public class MeasureParameterInfoList {

	private List<MeasureParameterInfo> parameterInfoList = new ArrayList<MeasureParameterInfo>();
	/**
	 * @param parameterInfoList MeasureParameterInfo objects
	 * @return a MeasureParameterInfoList object
	 */
	public MeasureParameterInfoList measureParameterInfoList(List<MeasureParameterInfo> parameterInfoList) {
		this.parameterInfoList = parameterInfoList;
		return this;
	}

	@ApiModelProperty(required = true, value = "A list of parameter information objects for libraries referenced by the input measure")
	@NotNull
	/**
	 * Get the parameter info list
	 * @return the parameter info list
	 */
	public List<MeasureParameterInfo> getParameterInfoList() {
		return parameterInfoList;
	}

	/**
	 * Set the parameter info list
	 * @param parameterInfoList the parameter info list
	 */
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
	 * @param o object to render
	 * @return object rendered as String with indentation
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
