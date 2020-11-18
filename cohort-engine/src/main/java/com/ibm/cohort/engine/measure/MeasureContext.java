package com.ibm.cohort.engine.measure;

import java.util.Map;

public class MeasureContext {
	private final String measureId;
	private final Map<String, Object> parameters;

	public MeasureContext(String measureId, Map<String, Object> parameters) {
		this.measureId = measureId;
		this.parameters = parameters;
	}

	public String getMeasureId() {
		return measureId;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}
}
