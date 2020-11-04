/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

public interface EvaluationResultCallback extends ExpressionResultCallback {
	void onContextBegin(String contextId);
	void onContextComplete(String contextId);
}
