package com.ibm.cohort.cql.spark.data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlExpressionConfiguration;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class ContextColumnNamerTest {

	@Test
	public void testMultipleRequestsColumnNames() {
		CqlLibraryDescriptor libraryDescriptor1 = new CqlLibraryDescriptor();
		libraryDescriptor1.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setDescriptor(libraryDescriptor1);

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

		CqlExpressionConfiguration expressionConfiguration3 = new CqlExpressionConfiguration();
		expressionConfiguration3.setName("ijkl");
		expressionConfiguration3.setoutputColumn("A3");

		CqlExpressionConfiguration expressionConfiguration4 = new CqlExpressionConfiguration();
		expressionConfiguration4.setName("mnop");

		request2.setExpressions(new HashSet<>(Arrays.asList(expressionConfiguration3, expressionConfiguration4)));

		ContextColumnNamer contextColumnNamer = ContextColumnNamer.create(Arrays.asList(request, request2), "|");

		assertEquals("lib1|abcd", contextColumnNamer.getOutputColumn(request, "abcd"));
		assertEquals("A2", contextColumnNamer.getOutputColumn(request, "efgh"));

		assertEquals("A3", contextColumnNamer.getOutputColumn(request2, "ijkl"));
		assertEquals("lib2|mnop", contextColumnNamer.getOutputColumn(request2, "mnop"));
	}

	@Test
	public void testMultipleRequestsSameDefineDifferentOutputColumn() {
		String commonLibrary = "lib1";
		String commonDefine = "abcd";
		
		CqlLibraryDescriptor libraryDescriptor1 = new CqlLibraryDescriptor();
		libraryDescriptor1.setLibraryId(commonLibrary);

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setDescriptor(libraryDescriptor1);

		CqlExpressionConfiguration expressionConfiguration1 = new CqlExpressionConfiguration();
		expressionConfiguration1.setName(commonDefine);
		expressionConfiguration1.setoutputColumn("col1");

		request.setExpressions(new HashSet<>(Collections.singletonList(expressionConfiguration1)));

		CqlLibraryDescriptor libraryDescriptor2 = new CqlLibraryDescriptor();
		libraryDescriptor2.setLibraryId(commonLibrary);

		CqlEvaluationRequest request2 = new CqlEvaluationRequest();
		request2.setDescriptor(libraryDescriptor2);

		CqlExpressionConfiguration expressionConfiguration2 = new CqlExpressionConfiguration();
		expressionConfiguration2.setName(commonDefine);
		expressionConfiguration2.setoutputColumn("col2");

		request2.setExpressions(new HashSet<>(Collections.singletonList(expressionConfiguration2)));

		ContextColumnNamer contextColumnNamer = ContextColumnNamer.create(Arrays.asList(request, request2), "|");

		assertEquals("col1", contextColumnNamer.getOutputColumn(request, commonDefine));
		assertEquals("col2", contextColumnNamer.getOutputColumn(request2, commonDefine));
	}

	@Test
	public void testMultipleRequestsSameDefineSameDefaultOutputColumn() {
		String commonLibrary = "lib1";
		String commonDefine = "abcd";

		CqlLibraryDescriptor libraryDescriptor1 = new CqlLibraryDescriptor();
		libraryDescriptor1.setLibraryId(commonLibrary);

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setDescriptor(libraryDescriptor1);

		CqlExpressionConfiguration expressionConfiguration1 = new CqlExpressionConfiguration();
		expressionConfiguration1.setName(commonDefine);

		request.setExpressions(new HashSet<>(Collections.singletonList(expressionConfiguration1)));

		CqlLibraryDescriptor libraryDescriptor2 = new CqlLibraryDescriptor();
		libraryDescriptor2.setLibraryId(commonLibrary);

		CqlEvaluationRequest request2 = new CqlEvaluationRequest();
		request2.setDescriptor(libraryDescriptor2);

		CqlExpressionConfiguration expressionConfiguration2 = new CqlExpressionConfiguration();
		expressionConfiguration2.setName(commonDefine);

		request2.setExpressions(new HashSet<>(Collections.singletonList(expressionConfiguration2)));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ContextColumnNamer.create(Arrays.asList(request, request2), "|"));
		assertTrue(ex.getMessage().contains("Duplicate outputColumn lib1|abcd defined"));
	}
	
	@Test
	public void testGetOutputColumnForUnknownEvaluationRequest() {
		ContextColumnNamer contextColumnNamer = ContextColumnNamer.create(Collections.emptyList(), "|");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> contextColumnNamer.getOutputColumn(new CqlEvaluationRequest(), "define1"));
		assertTrue(ex.getMessage().contains("Cannot find column name data for the provided request"));
	}
}