/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;

public class DefaultSparkOutputColumnEncoder implements Serializable, SparkOutputColumnEncoder {
	
	private final String columnDelimieter;
	
	public DefaultSparkOutputColumnEncoder(String columnDelimieter) {
		this.columnDelimieter = columnDelimieter;
	}

	@Override
	public String getColumnName(CqlEvaluationRequest request, String defineName) {
		return String.join(columnDelimieter, request.getDescriptor().getLibraryId(), defineName);
	}
}
