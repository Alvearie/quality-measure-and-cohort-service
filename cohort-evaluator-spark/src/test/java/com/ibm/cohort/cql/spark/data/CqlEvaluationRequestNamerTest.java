package com.ibm.cohort.cql.spark.data;


import static junit.framework.TestCase.assertEquals;
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

public class CqlEvaluationRequestNamerTest {
	@Test
	public void testOneDefineDefaultNaming() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");
		
		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setExpressionsByNames(new HashSet<>(Collections.singletonList("abcd")));
		request.setDescriptor(libraryDescriptor);

		Map<String, String> defineToOutputNameMap = CqlEvaluationRequestNamer.getDefineToOutputNameMap(request, "|");
		
		Map<String, String> expectedResult = new HashMap<String, String>() {{
			put("abcd", "lib1|abcd");
		}};
		
		assertEquals(expectedResult, defineToOutputNameMap);
	}

	@Test
	public void testMultipleUniqueDefinesDefaultNaming() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setExpressionsByNames(new HashSet<>(Arrays.asList("abcd", "efgh")));
		request.setDescriptor(libraryDescriptor);

		Map<String, String> defineToOutputNameMap = CqlEvaluationRequestNamer.getDefineToOutputNameMap(request, "|");

		Map<String, String> expectedResult = new HashMap<String, String>() {{
			put("abcd", "lib1|abcd");
			put("efgh", "lib1|efgh");
		}};

		assertEquals(expectedResult, defineToOutputNameMap);
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

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> CqlEvaluationRequestNamer.getDefineToOutputNameMap(request, "|"));
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

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> CqlEvaluationRequestNamer.getDefineToOutputNameMap(request, "|"));
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

		Map<String, String> defineToOutputNameMap = CqlEvaluationRequestNamer.getDefineToOutputNameMap(request, "|");

		Map<String, String> expectedResult = new HashMap<String, String>() {{
			put("abcd", "A1");
			put("efgh", "A2");
		}};

		assertEquals(expectedResult, defineToOutputNameMap);
	}
}