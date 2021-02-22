/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink;

public class MeasureExecution {
	private String measureId;
	private String patientId;

	public MeasureExecution() { }

	public MeasureExecution(String measureId, String patientId) {
		this.measureId = measureId;
		this.patientId = patientId;
	}

	public String getMeasureId() {
		return measureId;
	}

	public void setMeasureId(String measureId) {
		this.measureId = measureId;
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
				"measureId=" + measureId +
				", patientId='" + patientId + '\'' +
				'}';
	}
}
