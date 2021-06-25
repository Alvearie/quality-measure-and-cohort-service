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

public class SingleEvaluationResultCallback implements EvaluationResultCallback {

//	public Boolean getSingleExpressionResult() {
//		return singleExpressionResult;
//	}

	public List<String> getPassingPatients(){
		return passingPatients;
	}

	private List<String> passingPatients = new ArrayList<>();
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
//		if(singleExpressionResult != null){
//			throw new RuntimeException("Unexpected additional result!");
//		}
		if( result != null ) {
			if (result instanceof Boolean) {
				if((Boolean) result){
					passingPatients.add(contextId);
				}
				logger.info(String.format("Expression: \"%s\", Result: %s", expression, result));
			}
		}
	}
}
