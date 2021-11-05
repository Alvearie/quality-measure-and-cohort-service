/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metadata;


import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Test;

import com.ibm.cohort.cql.spark.errors.EvaluationError;

public class BaseOutputMetadataWriterTest {
	@Test
	public void testSuccessWritten() {
		BaseOutputMetadataWriter spy = spy(BaseOutputMetadataWriter.class);
		EvaluationSummary evaluationSummary = new EvaluationSummary();
		spy.writeMetadata(evaluationSummary);
		
		verify(spy, times(1)).createSuccessMarker();
	}

	@Test
	public void testNoSuccessWritten() {
		BaseOutputMetadataWriter spy = spy(BaseOutputMetadataWriter.class);
		EvaluationSummary evaluationSummary = new EvaluationSummary();
		evaluationSummary.setErrorList(Collections.singletonList(new EvaluationError()));
		spy.writeMetadata(evaluationSummary);

		verify(spy, times(0)).createSuccessMarker();
	}
}