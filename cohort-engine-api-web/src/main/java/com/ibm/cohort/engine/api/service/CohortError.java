/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.api.service;

import com.ibm.watson.service.base.model.ServiceError;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;

public class CohortError extends ServiceError {
    public enum ErrorSource {
        FHIR_SERVER,
        COHORT_SERVICE
    }

    @XmlElement
    @ApiModelProperty("ErrorSource")
    protected ErrorSource errorSource;

    public CohortError(int code, String message, ErrorSource errorSource) {
        super(code, message);
        this.errorSource = errorSource;
    }
}
