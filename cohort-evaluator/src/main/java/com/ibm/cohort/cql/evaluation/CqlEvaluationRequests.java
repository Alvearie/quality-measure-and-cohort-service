package com.ibm.cohort.cql.evaluation;

import java.util.List;
import java.util.Map;

public class CqlEvaluationRequests {
    private Map<String,Object> globalParameters;
    private List<CqlEvaluationRequest> evaluations;
    public Map<String, Object> getGlobalParameters() {
        return globalParameters;
    }
    public void setGlobalParameters(Map<String, Object> globalParameters) {
        this.globalParameters = globalParameters;
    }
    public List<CqlEvaluationRequest> getEvaluations() {
        return evaluations;
    }
    public void setEvaluations(List<CqlEvaluationRequest> evaluations) {
        this.evaluations = evaluations;
    }
}
