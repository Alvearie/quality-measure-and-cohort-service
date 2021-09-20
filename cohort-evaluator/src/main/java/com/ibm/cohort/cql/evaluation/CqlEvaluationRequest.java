package com.ibm.cohort.cql.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class CqlEvaluationRequest {
    private CqlLibraryDescriptor descriptor;
	private Set<String> expressions;
	@Valid
	private Map<String,Parameter> parameters;
	private String contextKey;
	private String contextValue;
	
	public CqlEvaluationRequest() {
	    super();
	}
	
	public CqlEvaluationRequest(CqlEvaluationRequest other) {
	    this.setDescriptor(other.getDescriptor());
	    this.setContextKey(other.getContextKey());
	    this.setContextValue(other.getContextValue());
	    if( other.getExpressions() != null ) {
	        this.setExpressions(new HashSet<>(other.getExpressions()));    
	    }
	    if( other.getParameters() != null ) {
	        this.setParameters(new HashMap<>(other.getParameters()));
	    }
	}
	
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
	public Map<String, Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, Parameter> parameters) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		CqlEvaluationRequest that = (CqlEvaluationRequest) o;

		return new EqualsBuilder()
				.append(descriptor, that.descriptor)
				.append(expressions, that.expressions)
				.append(parameters, that.parameters)
				.append(contextKey, that.contextKey)
				.append(contextValue, that.contextValue)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(descriptor)
				.append(expressions)
				.append(parameters)
				.append(contextKey)
				.append(contextValue)
				.toHashCode();
	}
}
