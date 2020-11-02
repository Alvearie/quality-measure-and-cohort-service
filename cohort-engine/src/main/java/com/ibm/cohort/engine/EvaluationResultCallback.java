package com.ibm.cohort.engine;

public interface EvaluationResultCallback extends ExpressionResultCallback {
	void onContextBegin(String contextId);
	void onContextComplete(String contextId);
}
