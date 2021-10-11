package com.ibm.cohort.cql.spark.data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.evaluation.CqlExpressionConfiguration;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class ConfigurableOutputColumnNameEncoderTest {
	@Test
	public void testNamesForSingleContext() {
		CqlLibraryDescriptor libraryDescriptor1 = new CqlLibraryDescriptor();
		libraryDescriptor1.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setDescriptor(libraryDescriptor1);
		request.setId(1);

		CqlExpressionConfiguration expressionConfiguration1 = new CqlExpressionConfiguration();
		expressionConfiguration1.setName("abcd");

		CqlExpressionConfiguration expressionConfiguration2 = new CqlExpressionConfiguration();
		expressionConfiguration2.setName("efgh");
		expressionConfiguration2.setoutputColumn("A2");

		request.setExpressions(new HashSet<>(Arrays.asList(expressionConfiguration1, expressionConfiguration2)));

		CqlLibraryDescriptor libraryDescriptor2 = new CqlLibraryDescriptor();
		libraryDescriptor2.setLibraryId("lib2");

		CqlEvaluationRequest request2 = new CqlEvaluationRequest();
		request2.setDescriptor(libraryDescriptor2);
		request2.setId(2);

		CqlExpressionConfiguration expressionConfiguration3 = new CqlExpressionConfiguration();
		expressionConfiguration3.setName("ijkl");
		expressionConfiguration3.setoutputColumn("A3");

		CqlExpressionConfiguration expressionConfiguration4 = new CqlExpressionConfiguration();
		expressionConfiguration4.setName("mnop");

		request2.setExpressions(new HashSet<>(Arrays.asList(expressionConfiguration3, expressionConfiguration4)));

		request.setContextKey("context1");
		request2.setContextKey("context1");

		CqlEvaluationRequests evaluationRequests = new CqlEvaluationRequests();
		evaluationRequests.setEvaluations(Arrays.asList(request, request2));
		
		ConfigurableOutputColumnNameEncoder nameEncoder = ConfigurableOutputColumnNameEncoder.create(evaluationRequests, "|");

		assertEquals("lib1|abcd", nameEncoder.getColumnName(request, "abcd"));
		assertEquals("A2", nameEncoder.getColumnName(request, "efgh"));
		assertEquals("A3", nameEncoder.getColumnName(request2, "ijkl"));
		assertEquals("lib2|mnop", nameEncoder.getColumnName(request2, "mnop"));
	}

	@Test
	public void testNamesForMultipleContexts() {
		CqlLibraryDescriptor libraryDescriptor1 = new CqlLibraryDescriptor();
		libraryDescriptor1.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setDescriptor(libraryDescriptor1);
		request.setId(1);

		CqlExpressionConfiguration expressionConfiguration1 = new CqlExpressionConfiguration();
		expressionConfiguration1.setName("abcd");
		expressionConfiguration1.setoutputColumn("A1");

		CqlExpressionConfiguration expressionConfiguration2 = new CqlExpressionConfiguration();
		expressionConfiguration2.setName("efgh");
		expressionConfiguration2.setoutputColumn("A2");

		request.setExpressions(new HashSet<>(Arrays.asList(expressionConfiguration1, expressionConfiguration2)));

		CqlLibraryDescriptor libraryDescriptor2 = new CqlLibraryDescriptor();
		libraryDescriptor2.setLibraryId("lib1");

		CqlEvaluationRequest request2 = new CqlEvaluationRequest();
		request2.setDescriptor(libraryDescriptor2);
		request2.setId(2);

		CqlExpressionConfiguration expressionConfiguration3 = new CqlExpressionConfiguration();
		expressionConfiguration3.setName("abcd");
		expressionConfiguration3.setoutputColumn("A3");

		CqlExpressionConfiguration expressionConfiguration4 = new CqlExpressionConfiguration();
		expressionConfiguration4.setName("efgh");
		expressionConfiguration4.setoutputColumn("A4");

		request2.setExpressions(new HashSet<>(Arrays.asList(expressionConfiguration3, expressionConfiguration4)));

		request.setContextKey("context1");
		request2.setContextKey("context2");

		CqlEvaluationRequests evaluationRequests = new CqlEvaluationRequests();
		evaluationRequests.setEvaluations(Arrays.asList(request, request2));

		ConfigurableOutputColumnNameEncoder nameEncoder = ConfigurableOutputColumnNameEncoder.create(evaluationRequests, "|");

		assertEquals("A1", nameEncoder.getColumnName(request, "abcd"));
		assertEquals("A2", nameEncoder.getColumnName(request, "efgh"));
		assertEquals("A3", nameEncoder.getColumnName(request2, "abcd"));
		assertEquals("A4", nameEncoder.getColumnName(request2, "efgh"));
	}

	@Test
	public void testOutputColumnsRepeatedAcrossContextsThrowsError() {
		CqlLibraryDescriptor libraryDescriptor1 = new CqlLibraryDescriptor();
		libraryDescriptor1.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setDescriptor(libraryDescriptor1);
		request.setId(1);

		CqlExpressionConfiguration expressionConfiguration1 = new CqlExpressionConfiguration();
		expressionConfiguration1.setName("abcd");
		expressionConfiguration1.setoutputColumn("A1");
		
		
		request.setExpressions(new HashSet<>(Collections.singletonList(expressionConfiguration1)));

		CqlLibraryDescriptor libraryDescriptor2 = new CqlLibraryDescriptor();
		libraryDescriptor2.setLibraryId("lib2");

		CqlEvaluationRequest request2 = new CqlEvaluationRequest();
		request2.setDescriptor(libraryDescriptor2);
		request2.setId(2);

		CqlExpressionConfiguration expressionConfiguration3 = new CqlExpressionConfiguration();
		expressionConfiguration3.setName("abcd");
		expressionConfiguration3.setoutputColumn("A1");

		request2.setExpressions(new HashSet<>(Collections.singletonList(expressionConfiguration3)));

		request.setContextKey("context1");
		request2.setContextKey("context2");

		CqlEvaluationRequests evaluationRequests = new CqlEvaluationRequests();
		evaluationRequests.setEvaluations(Arrays.asList(request, request2));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ConfigurableOutputColumnNameEncoder.create(evaluationRequests, "|"));
		assertTrue(ex.getMessage().contains("Output column A1 defined multiple times"));
	}
}