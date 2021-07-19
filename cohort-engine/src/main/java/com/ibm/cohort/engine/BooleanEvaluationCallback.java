/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.engine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

public class BooleanEvaluationCallback implements EvaluationResultCallback {


	public List<String> getPassingPatients(){
		return passingPatients;
	}

	private List<String> passingPatients = new ArrayList<>();
	private static final Logger logger = LoggerFactory.getLogger(BooleanEvaluationCallback.class.getName());

	@Override
	public void onContextBegin(String contextId) {
		logger.info("Context: " + contextId);
	}

	@Override
	public void onContextComplete(String contextId) {
		logger.info(contextId + " complete");
		logger.info("---");
	}

	@Override
	public void onEvaluationComplete(String contextId, String expression, Object result) {
		if( result != null ) {
			if (result instanceof Boolean) {
				if((Boolean) result){
					passingPatients.add(contextId);
				}
				logger.info(String.format("Expression: \"%s\", Result: %s, ContextId: %s", expression, result, contextId));
			}
			else {
				throw new RuntimeException(String.format("Only boolean CQLs are currently supported! Expression: \"%s\", Result: %s, ContextId: %s", expression, result, contextId));
			}
		} else {
			throw new RuntimeException(String.format("Null result is unsupported! Expression: \"%s\", Result: %s, ContextId: %s", expression, result, contextId));
		}
	}
}
