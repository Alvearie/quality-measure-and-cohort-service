/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.Map;

import org.hl7.fhir.r4.model.Identifier;

public class MeasureContext {
	private final String measureId;
	private final Map<String, Object> parameters;
	private final Identifier identifier;

	public MeasureContext(String measureId, Map<String, Object> parameters, Identifier  identifier) {
		this.measureId = measureId;
		this.parameters = parameters;
		this.identifier = identifier;
	}

	public String getMeasureId() {
		return measureId;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public Identifier getIdentifier() {
		return identifier;
	}
}
