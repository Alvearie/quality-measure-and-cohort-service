package com.ibm.cohort.cql.spark.data;


import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class OutputColumnNamerTest {
	
	@Test
	public void fakeTest() {
		CqlEvaluationRequest request1 = new CqlEvaluationRequest();
		request1.setContextKey("A");
		request1.setDescriptor(new CqlLibraryDescriptor().setLibraryId("L1").setVersion("1.0.0"));
		request1.setExpressions(new HashSet<>(Arrays.asList("d1", "d2")));

		CqlEvaluationRequest request2 = new CqlEvaluationRequest();
		request2.setContextKey("A");
		request2.setDescriptor(new CqlLibraryDescriptor().setLibraryId("L2").setVersion("1.0.0"));
		request2.setExpressions(new HashSet<>(Arrays.asList("d1", "d2")));

		CqlEvaluationRequest request3 = new CqlEvaluationRequest();
		request3.setContextKey("B");
		request3.setDescriptor(new CqlLibraryDescriptor().setLibraryId("L3").setVersion("1.0.0"));
		request3.setExpressions(new HashSet<>(Arrays.asList("d1", "d2")));

		CqlEvaluationRequests requests = new CqlEvaluationRequests();
		requests.setEvaluations(Arrays.asList(request1, request2, request3));
		
		OutputColumnNamer.create(requests, "|");
	}
}