package com.ibm.cohort.cql.evaluation;

import java.util.List;
import java.util.Map;

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
}
