/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class CqlEvaluationRequest {
	@JsonIgnore
	private Integer id;
    private CqlLibraryDescriptor descriptor;
	private Set<CqlExpressionConfiguration> expressions;
	@Valid
	private Map<String,Parameter> parameters;
	private String contextKey;
	private String contextValue;
	
	public CqlEvaluationRequest() {
	    super();
	}
	
	public CqlEvaluationRequest(CqlEvaluationRequest other) {
		this.setId(other.getId());
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

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public CqlLibraryDescriptor getDescriptor() {
        return descriptor;
    }
    public void setDescriptor(CqlLibraryDescriptor descriptor) {
        this.descriptor = descriptor;
    }
	public Set<CqlExpressionConfiguration> getExpressions() {
		return expressions;
	}
	public void setExpressions(Set<CqlExpressionConfiguration> expressions) {
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
    
    public Set<String> getExpressionNames() {
		return expressions.stream().map(CqlExpressionConfiguration::getName).collect(Collectors.toSet());
	}
	
	public void setExpressionsByNames(Set<String> expressionNames) {
		Set<CqlExpressionConfiguration> expressionConfigurations = new HashSet<>();
		for (String expressionName : expressionNames) {
			CqlExpressionConfiguration expressionConfiguration = new CqlExpressionConfiguration();
			expressionConfiguration.setName(expressionName);
			expressionConfigurations.add(expressionConfiguration);
		}
		
		this.expressions = expressionConfigurations;
	}
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("descriptor", descriptor)
                .append("expressions", expressions)
                .append("parameters", parameters)
                .append("contextKey", contextKey)
                .append("contextValue", contextValue)
                .toString();
    }
}
