/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.cohort.annotations.Generated;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "Payload containing list of patients passing a CohortEvaluation")
@Generated
public class CohortResult {

  private List<String> result = new ArrayList<>();

  public CohortResult() {
  }

  public CohortResult(List<String> result) {
    this.result = result;
  }

  @ApiModelProperty(value = "list of patients", required = true)
  @JsonProperty("result")
  @Valid
  public List<String> getResult() {
    return result;
  }

  public void setResult(List<String> result) {
    this.result = result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CohortResult that = (CohortResult) o;
    return Objects.equals(result, that.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result);
  }

  @Override
  public String toString() {
    return "CohortResult{" +
      "result=" + result +
      '}';
  }
}


