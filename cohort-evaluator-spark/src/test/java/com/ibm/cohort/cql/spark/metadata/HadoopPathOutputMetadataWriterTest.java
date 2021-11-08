/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metadata;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import com.ibm.cohort.cql.spark.errors.EvaluationError;

public class HadoopPathOutputMetadataWriterTest {
	
	@Test
	public void testSuccessMakerCreated() {
		String outputPath = "target/output/metadata/testcreate";
		Path metadataPath = new Path(outputPath);
		
		HadoopPathOutputMetadataWriter writer = new HadoopPathOutputMetadataWriter(metadataPath, new Configuration());
		EvaluationSummary evaluationSummary = new EvaluationSummary();
		evaluationSummary.setApplicationId("id123");
		
		writer.writeMetadata(evaluationSummary);
		assertTrue(new File(outputPath, HadoopPathOutputMetadataWriter.SUCCESS_MARKER).exists());
		assertTrue(new File(outputPath, HadoopPathOutputMetadataWriter.BATCH_SUMMARY_PREFIX + "id123").exists());
	}

	@Test
	public void testNoSuccessMarkerCreated() {
		String outputPath = "target/output/metadata/testnosuccess";
		Path metadataPath = new Path(outputPath);
		EvaluationSummary evaluationSummary = new EvaluationSummary();
		evaluationSummary.setApplicationId("id456");
		evaluationSummary.setErrorList(Collections.singletonList(new EvaluationError()));


		HadoopPathOutputMetadataWriter writer = new HadoopPathOutputMetadataWriter(metadataPath, new Configuration());
		writer.writeMetadata(evaluationSummary);
		assertFalse(new File(outputPath, HadoopPathOutputMetadataWriter.SUCCESS_MARKER).exists());
		assertTrue(new File(outputPath, HadoopPathOutputMetadataWriter.BATCH_SUMMARY_PREFIX + "id456").exists());
	}
}