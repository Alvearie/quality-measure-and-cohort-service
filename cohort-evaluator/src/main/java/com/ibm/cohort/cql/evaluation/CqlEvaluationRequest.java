package com.ibm.cohort.cql.evaluation;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class CqlEvaluationRequest {
	private String libraryId;
	private String libraryVersion;
	private Set<String> expressions;
	private Map<String,Object> parameters;
	private Pair<String,String> context;
	
	public String getLibraryId() {
		return libraryId;
	}
	public void setLibraryId(String libraryId) {
		this.libraryId = libraryId;
	}
	public String getLibraryVersion() {
		return libraryVersion;
	}
	public void setLibraryVersion(String libraryVersion) {
		this.libraryVersion = libraryVersion;
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
	public Pair<String, String> getContext() {
		return context;
	}
	public void setContext(Pair<String, String> context) {
		this.context = context;
	}
	
}
