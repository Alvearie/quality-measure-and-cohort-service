/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.ibm.cohort.cql.evaluation.parameters.Parameter;

public class CqlEvaluationRequests {
    @Valid
    private Map<String,Parameter> globalParameters;
    
    @NotNull
    @Size( min = 1 )
    @Valid
    private List<CqlEvaluationRequest> evaluations;
    
    public Map<String, Parameter> getGlobalParameters() {
        return globalParameters;
    }
    public void setGlobalParameters(Map<String, Parameter> globalParameters) {
        this.globalParameters = globalParameters;
    }
    public List<CqlEvaluationRequest> getEvaluations() {
        return evaluations;
    }
    public void setEvaluations(List<CqlEvaluationRequest> evaluations) {
        this.evaluations = evaluations;
    }

    /**
     * Retrieves a list of CqlEvaluationRequest objects with the provided contextName.
     * 
     * @param contextName   The context name used to retrieve requests.
     * @return  A list of CqlEvaluationRequests containing the provided contextName.
     *          An empty list is returned if no such requests are found.
     */
    public List<CqlEvaluationRequest> getEvaluationsForContext(String contextName) {
        return evaluations.stream().filter(r -> r.getContextKey() != null && r.getContextKey().equals(contextName)).collect(Collectors.toList());
    }
}
