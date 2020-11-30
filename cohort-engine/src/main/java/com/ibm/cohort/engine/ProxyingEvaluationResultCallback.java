/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

/**
 * Provide a bridge between the functional ExpressionResultCallback and the
 * new EvaluationResultCallback method so that clients don't have to change
 * a lot of their code that uses the ExpressionResultCallback.
 */
public class ProxyingEvaluationResultCallback implements EvaluationResultCallback {

	private ExpressionResultCallback proxy;

	public ProxyingEvaluationResultCallback(ExpressionResultCallback proxy) {
		this.proxy = proxy;
	}
	
	@Override
	public void onEvaluationComplete(String contextId, String expression, Object result) {
		proxy.onEvaluationComplete(contextId, expression, result);
	}

	@Override
	public void onContextBegin(String contextId) {
		// do nothing
	}

	@Override
	public void onContextComplete(String contextId) {
		// do nothing
	}

}
