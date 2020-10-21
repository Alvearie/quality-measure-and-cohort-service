/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

/**
 * Callback function that will be called for each context passed to the CqlEngineWrapper.
 */
@FunctionalInterface
public interface EvaluationResultCallback {
	public void onEvaluationComplete( String contextId, String expression, Object result );
}
