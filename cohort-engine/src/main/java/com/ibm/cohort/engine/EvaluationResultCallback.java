/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

/**
 * Callback function that provides callout points at the beginning and
 * end of each context evaluation.
 */
public interface EvaluationResultCallback extends ExpressionResultCallback {
	void onContextBegin(String contextId);
	void onContextComplete(String contextId);
}
