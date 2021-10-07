package com.ibm.cohort.cql.spark.data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlExpressionConfiguration;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

import junit.framework.TestCase;

public class ContextColumnNameEncoderTest {

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

		ContextColumnNameEncoder contextColumnNameEncoder = ContextColumnNameEncoder.create(Arrays.asList(request, request2), "|");

		assertEquals("lib1|abcd", contextColumnNameEncoder.getColumnName(request, "abcd"));
		assertEquals("A2", contextColumnNameEncoder.getColumnName(request, "efgh"));

		assertEquals("A3", contextColumnNameEncoder.getColumnName(request2, "ijkl"));
		assertEquals("lib2|mnop", contextColumnNameEncoder.getColumnName(request2, "mnop"));
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

		ContextColumnNameEncoder contextColumnNameEncoder = ContextColumnNameEncoder.create(Arrays.asList(request, request2), "|");

		assertEquals("col1", contextColumnNameEncoder.getColumnName(request, commonDefine));
		assertEquals("col2", contextColumnNameEncoder.getColumnName(request2, commonDefine));
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

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ContextColumnNameEncoder.create(Arrays.asList(request, request2), "|"));
		assertTrue(ex.getMessage().contains("Duplicate outputColumn lib1|abcd defined"));
	}
	
	@Test
	public void testgetColumnNameForUnknownEvaluationRequest() {
		ContextColumnNameEncoder contextColumnNameEncoder = ContextColumnNameEncoder.create(Collections.emptyList(), "|");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> contextColumnNameEncoder.getColumnName(new CqlEvaluationRequest(), "define1"));
		assertTrue(ex.getMessage().contains("Cannot find column name data for the provided request"));
	}

	@Test
	public void testOneDefineDefaultNaming() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setExpressionsByNames(new HashSet<>(Collections.singletonList("abcd")));
		request.setDescriptor(libraryDescriptor);

		Map<String, String> defineToOutputNameMap = ContextColumnNameEncoder.getDefineToOutputNameMap(request, "|");

		Map<String, String> expectedResult = new HashMap<String, String>() {{
			put("abcd", "lib1|abcd");
		}};

		TestCase.assertEquals(expectedResult, defineToOutputNameMap);
	}

	@Test
	public void testMultipleUniqueDefinesDefaultNaming() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setExpressionsByNames(new HashSet<>(Arrays.asList("abcd", "efgh")));
		request.setDescriptor(libraryDescriptor);

		Map<String, String> defineToOutputNameMap = ContextColumnNameEncoder.getDefineToOutputNameMap(request, "|");

		Map<String, String> expectedResult = new HashMap<String, String>() {{
			put("abcd", "lib1|abcd");
			put("efgh", "lib1|efgh");
		}};

		TestCase.assertEquals(expectedResult, defineToOutputNameMap);
	}

	@Test
	public void testMultipleDefinesSameNameThrowsError() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");

		CqlExpressionConfiguration expressionConfiguration1 = new CqlExpressionConfiguration();
		expressionConfiguration1.setName("abcd");
		expressionConfiguration1.setoutputColumn("A1");

		CqlExpressionConfiguration expressionConfiguration2 = new CqlExpressionConfiguration();
		expressionConfiguration2.setName("abcd");
		expressionConfiguration2.setoutputColumn("A2");


		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setExpressions(new HashSet<>(Arrays.asList(expressionConfiguration1, expressionConfiguration2)));
		request.setDescriptor(libraryDescriptor);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ContextColumnNameEncoder.getDefineToOutputNameMap(request, "|"));
		assertTrue(ex.getMessage().contains("Evaluation request contains duplicate expression abcd"));
	}

	@Test
	public void testMultipleDefinesSameOutputColumnThrowsError() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");

		CqlExpressionConfiguration expressionConfiguration1 = new CqlExpressionConfiguration();
		expressionConfiguration1.setName("abcd");
		expressionConfiguration1.setoutputColumn("NAME");

		CqlExpressionConfiguration expressionConfiguration2 = new CqlExpressionConfiguration();
		expressionConfiguration2.setName("efgh");
		expressionConfiguration2.setoutputColumn("NAME");


		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setExpressions(new HashSet<>(Arrays.asList(expressionConfiguration1, expressionConfiguration2)));
		request.setDescriptor(libraryDescriptor);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ContextColumnNameEncoder.getDefineToOutputNameMap(request, "|"));
		assertTrue(ex.getMessage().contains("Evaluation request contains duplicate outputColumn"));
	}

	@Test
	public void testDefinesWithOutputColumns() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();

		CqlExpressionConfiguration expressionConfiguration1 = new CqlExpressionConfiguration();
		expressionConfiguration1.setName("abcd");
		expressionConfiguration1.setoutputColumn("A1");

		CqlExpressionConfiguration expressionConfiguration2 = new CqlExpressionConfiguration();
		expressionConfiguration2.setName("efgh");
		expressionConfiguration2.setoutputColumn("A2");

		request.setExpressions(new HashSet<>(Arrays.asList(expressionConfiguration1, expressionConfiguration2)));
		request.setDescriptor(libraryDescriptor);

		Map<String, String> defineToOutputNameMap = ContextColumnNameEncoder.getDefineToOutputNameMap(request, "|");

		Map<String, String> expectedResult = new HashMap<String, String>() {{
			put("abcd", "A1");
			put("efgh", "A2");
		}};

		TestCase.assertEquals(expectedResult, defineToOutputNameMap);
	}
}