/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.engine;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

public class SingleEvaluationResultCallback implements EvaluationResultCallback {

	public Boolean getSingleExpressionResult() {
		return singleExpressionResult;
	}

	private Boolean singleExpressionResult = null;
	private static final Logger logger = LoggerFactory.getLogger(SingleEvaluationResultCallback.class.getName());

	@Override
	public void onContextBegin(String contextId) {
		logger.info("Context: " + contextId);
	}

	@Override
	public void onContextComplete(String contextId) {
		logger.info("---");
	}

	@Override
	public void onEvaluationComplete(String contextId, String expression, Object result) {
		if(singleExpressionResult != null){
			throw new RuntimeException("Unexpected additional result!");
		}
		if( result != null ) {
			if (result instanceof Boolean) {
				singleExpressionResult = (Boolean) result;
				logger.info(String.format("Expression: \"%s\", Result: %s", expression, singleExpressionResult));
			}
		}
	}
}
