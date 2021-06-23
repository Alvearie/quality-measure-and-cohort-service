/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.engine.api.service;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import com.ibm.cohort.engine.EvaluationResultCallback;

public class MoveThisLaterCallback implements EvaluationResultCallback {

	Boolean singleExpressionResult = null;
	private static final Logger logger = LoggerFactory.getLogger(MoveThisLaterCallback.class.getName());

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
		if( result != null ) {
			if (result instanceof Boolean) {
				singleExpressionResult = (Boolean) result;
				logger.info(String.format("Expression: \"%s\", Result: %s", expression, singleExpressionResult));
			}
		}
	}
}
