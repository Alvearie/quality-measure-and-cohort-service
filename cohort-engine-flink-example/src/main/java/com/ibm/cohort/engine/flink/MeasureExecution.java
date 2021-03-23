/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink;

import java.util.List;

public class MeasureExecution {
	private List<String> measureIds;
	private String patientId;

	public MeasureExecution() { }

	public MeasureExecution(List<String> measureIds, String patientId) {
		this.measureIds = measureIds;
		this.patientId = patientId;
	}

	public List<String> getMeasureIds() {
		return measureIds;
	}

	public void setMeasureIds(List<String> measureIds) {
		this.measureIds = measureIds;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	@Override
	public String toString() {
		return "MeasureExecution{" +
				"measureIds=" + measureIds +
				", patientId='" + patientId + '\'' +
				'}';
	}
}
