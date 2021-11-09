package com.ibm.cohort.cql.spark.metadata;


import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.spark.errors.EvaluationError;

public class EvaluationSummaryTest {

	@Test
	public void testObjectSerialization() throws JsonProcessingException {
		EvaluationSummary evaluationSummaryOrig = new EvaluationSummary();
		evaluationSummaryOrig.setApplicationId("123");
		evaluationSummaryOrig.setStartTimeMillis(1000);
		evaluationSummaryOrig.setEndTimeMillis(50000);
		evaluationSummaryOrig.setTotalContexts(1);
		evaluationSummaryOrig.setErrorList(Collections.singletonList(new EvaluationError()));
		evaluationSummaryOrig.setExecutionsPerContext(new HashMap<String, Long>(){{put("contextA", 2L);}});
		evaluationSummaryOrig.setSecondsPerContext(new HashMap<String, Long>(){{put("contextA", 40L);}});

		ObjectMapper mapper = new ObjectMapper();
		String stringVal = mapper.writeValueAsString(evaluationSummaryOrig);
		EvaluationSummary evaluationSummaryNew = mapper.readValue(stringVal, EvaluationSummary.class);
		
		// We expect runtimeSeconds to be set after serializing/deserializing
		evaluationSummaryOrig.setRuntimeSeconds(49);
		
		assertEquals(evaluationSummaryOrig, evaluationSummaryNew);
	}
}