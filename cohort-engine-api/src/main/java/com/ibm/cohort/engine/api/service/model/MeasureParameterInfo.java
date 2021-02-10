/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@Generated
public class MeasureParameterInfo {

	private String name = null;
	private String use = null;
	private Integer min = null;
	private String max = null;
	private String type = null;
	private String documentation = new String();

	/**
	 * @param name Name of the parameter which is the Fhir ParameterDefinition.name field
	 * @return
	 */
	public MeasureParameterInfo name(String name) {
		this.name = name;
		return this;
	}

	@ApiModelProperty(value = "Name of the parameter which is the Fhir ParameterDefinition.name field")
	@JsonProperty("name")
	public String getName() {
		return name;
	}

	/**
	 * @param name Name of the parameter which is the Fhir ParameterDefinition.name field
	 */
	public void setname(String name) {
		this.name = name;
	}

	/**
	 * @param use A string describing if the parameter is an input or output parameter. FHIR ParameterDefinition.use field
	 * @return
	 */
	public MeasureParameterInfo use(String use) {
		this.use = use;
		return this;
	}

	@ApiModelProperty(value = "A string describing if the parameter is an input or output parameter. FHIR ParameterDefinition.use field")
	@JsonProperty("use")
	public String getUse() {
		return use;
	}

	/**
	 * @param use A string describing if the parameter is an input or output parameter. FHIR ParameterDefinition.use field 
	 */
	public void setUse(String use) {
		this.use = use;
	}

	/**
	 * @param max A string representing the maximum number of times this parameter may be used. FHIR ParameterDefinition.max field
	 * @return a MeasureParameterInfo object
	 */
	public MeasureParameterInfo max(String max) {
		this.max = max;
		return this;
	}

	@ApiModelProperty(value = "A string representing the maximum number of times this parameter may be used. FHIR ParameterDefinition.max field")
	@JsonProperty("max")
	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	/**
	 * @param type The type of the parameter. FHIR ParameterDefinition.type field
	 * @return
	 */
	public MeasureParameterInfo type(String type) {
		this.type = type;
		return this;
	}

	@ApiModelProperty(value = "The type of the parameter. FHIR ParameterDefinition.type field")
	@JsonProperty("type")
	public String getType() {
		return type;
	}

	/**
	 * @param type The type of the parameter. FHIR ParameterDefinition.type field
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param documentation A string describing any documentation associated with this parameter. FHIR FHIR ParameterDefinition.documentation field
	 * @return
	 */
	public MeasureParameterInfo documentation(String documentation) {
		this.documentation = documentation;
		return this;
	}

	@ApiModelProperty(value = "A string describing any documentation associated with this parameter. FHIR FHIR ParameterDefinition.documentation field")
	@JsonProperty("documentation")
	public String getDocumentation() {
		return documentation;
	}

	/**
	 * @param documentation A string describing any documentation associated with this parameter. FHIR FHIR ParameterDefinition.documentation field
	 */
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	/**
	 * @param min The minimum number of times this parameter may be used (ie 0 means optional parameter,  greater than or equal to 1 means required parameter) FHIR ParameterDefinition.min field
	 * @return
	 */
	public MeasureParameterInfo min(Integer min) {
		this.min = min;
		return this;
	}

	@ApiModelProperty(value = "The minimum number of times this parameter may be used (ie 0 means optional parameter, greater than or equal to 1 means required parameter) FHIR ParameterDefinition.min field")
	@JsonProperty("min")
	public Integer getMin() {
		return min;
	}

	/**
	 * @param min The minimum number of times this parameter may be used (ie 0 means optional parameter,  greater than or equal to 1 means required parameter) FHIR ParameterDefinition.min field
	 */
	public void setMin(Integer min) {
		this.min = min;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MeasureParameterInfo measureParameterInfo = (MeasureParameterInfo) o;
		return Objects.equals(name, measureParameterInfo.name)
				&& Objects.equals(use, measureParameterInfo.use)
				&& Objects.equals(max, measureParameterInfo.max)
				&& Objects.equals(type, measureParameterInfo.type)
				&& Objects.equals(documentation,
						measureParameterInfo.documentation)
				&& Objects.equals(min, measureParameterInfo.min);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, use, max,
				type, documentation, min);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class MeasureParameterInfo {\n");

		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    use: ").append(toIndentedString(use)).append("\n");
		sb.append("    min: ").append(toIndentedString(min)).append("\n");
		sb.append("    max: ").append(toIndentedString(max)).append("\n");
		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("    documentation: ").append(toIndentedString(documentation))
				.append("\n");
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
