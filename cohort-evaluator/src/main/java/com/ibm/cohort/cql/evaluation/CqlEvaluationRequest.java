package com.ibm.cohort.cql.evaluation;

import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class CqlEvaluationRequest {
    private CqlLibraryDescriptor descriptor;
	private Set<String> expressions;
	private Map<String,Object> parameters;
	private String contextKey;
	private String contextValue;
	
    public CqlLibraryDescriptor getDescriptor() {
        return descriptor;
    }
    public void setDescriptor(CqlLibraryDescriptor descriptor) {
        this.descriptor = descriptor;
    }
	public Set<String> getExpressions() {
		return expressions;
	}
	public void setExpressions(Set<String> expressions) {
		this.expressions = expressions;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
    public String getContextKey() {
        return contextKey;
    }
    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }
    public String getContextValue() {
        return contextValue;
    }
    public void setContextValue(String contextValue) {
        this.contextValue = contextValue;
    }
}
