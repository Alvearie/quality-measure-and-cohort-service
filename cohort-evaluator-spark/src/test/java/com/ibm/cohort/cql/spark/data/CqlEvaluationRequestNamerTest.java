package com.ibm.cohort.cql.spark.data;


import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class CqlEvaluationRequestNamerTest {
	@Test
	public void testOneDefineDefaultNaming() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");
		
		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setExpressions(new HashSet<>(Collections.singletonList("abcd")));
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
		request.setExpressions(new HashSet<>(Arrays.asList("abcd", "efgh")));
		request.setDescriptor(libraryDescriptor);

		Map<String, String> defineToOutputNameMap = CqlEvaluationRequestNamer.getDefineToOutputNameMap(request, "|");

		Map<String, String> expectedResult = new HashMap<String, String>() {{
			put("abcd", "lib1|abcd");
			put("efgh", "lib1|efgh");
		}};

		assertEquals(expectedResult, defineToOutputNameMap);
	}

	// TODO: Cannot set up this test yet with strings as input. Need complex define -> output name objects first
	// OR -- Mappings can live elsewhere. That would allow existing jobs files to be compatible with changes.
	@Ignore
	@Test
	public void testMultipleDefinesSameNameThrowsError() {
		CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor();
		libraryDescriptor.setLibraryId("lib1");

		CqlEvaluationRequest request = new CqlEvaluationRequest();
		request.setExpressions(new HashSet<>(Arrays.asList("abcd", "abcd")));
		request.setDescriptor(libraryDescriptor);

		assertThrows(IllegalArgumentException.class, () -> CqlEvaluationRequestNamer.getDefineToOutputNameMap(request, "|"));
	}
}