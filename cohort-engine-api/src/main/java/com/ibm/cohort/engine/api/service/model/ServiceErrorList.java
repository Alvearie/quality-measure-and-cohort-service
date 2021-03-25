/*
 * (C) Copyright IBM Corp. 2020, 2020
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
import com.ibm.watson.common.service.base.ServiceBaseUtility;
import com.ibm.watson.service.base.model.ServiceError;

/**
 * Object representing an HTTP response with errors
 **/
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Object representing an HTTP response with errors")

@Generated
public class ServiceErrorList   {
  
  private List<ServiceError> errors = new ArrayList<ServiceError>();
  private String trace = null;
  private Integer statusCode = null;
  
  /**
	 * Default constructor
	 */
	public ServiceErrorList() {
		initialize();
	}
	
	/**
	 * Initialize this error object with default values from thread local storage
	 * if present. This is typically only used on the server side when the ServiceError
	 * object is being built, not inflated from json.
	 */
	protected void initialize() {
		// Get the correlation ID if the server-side utility class is present.
		trace = ServiceBaseUtility.getCorrelationId();
	}
	

  /**
 * @param errors A list of input errors
 * @return this
 */
public ServiceErrorList errors(List<ServiceError> errors) {
    this.errors = errors;
    return this;
  }

  
  @ApiModelProperty(value = "Errors")
  @JsonProperty("errors")
  @Valid
  public List<ServiceError> getErrors() {
    return errors;
  }
  public void setErrors(List<ServiceError> errors) {
    this.errors = errors;
  }

  /**
 * @param trace error message correlation identifier
 * @return this
 */
public ServiceErrorList trace(String trace) {
    this.trace = trace;
    return this;
  }

  
  @ApiModelProperty(value = "error message correlation identifier")
  @JsonProperty("trace")
  public String getTrace() {
    return trace;
  }
  public void setTrace(String trace) {
    this.trace = trace;
  }

  /**
 * @param statusCode response code
 * @return this
 */
public ServiceErrorList statusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  
  @ApiModelProperty(value = "respone code")
  @JsonProperty("status_code")
  public Integer getStatusCode() {
    return statusCode;
  }
  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceErrorList serviceErrorList = (ServiceErrorList) o;
    return Objects.equals(errors, serviceErrorList.errors) &&
        Objects.equals(trace, serviceErrorList.trace) &&
        Objects.equals(statusCode, serviceErrorList.statusCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errors, trace, statusCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceErrorList {\n");
    
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    trace: ").append(toIndentedString(trace)).append("\n");
    sb.append("    statusCode: ").append(toIndentedString(statusCode)).append("\n");
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


