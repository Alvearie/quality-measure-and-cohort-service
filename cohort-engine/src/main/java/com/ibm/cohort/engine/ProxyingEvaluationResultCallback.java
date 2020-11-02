/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

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
