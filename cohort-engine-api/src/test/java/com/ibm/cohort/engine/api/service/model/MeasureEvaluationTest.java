/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.model;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.measure.MeasureContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.engine.parameter.DateParameter;
import com.ibm.cohort.engine.parameter.IntervalParameter;
import com.ibm.cohort.engine.parameter.Parameter;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class MeasureEvaluationTest {
	@Test
	public void when_serialize_deserialize___properties_are_unchanged() throws Exception {
		
		FhirServerConfig dataServerConfig = new FhirServerConfig();
		dataServerConfig.setEndpoint("dataserver");
		
		FhirServerConfig termServerConfig = new FhirServerConfig();
		termServerConfig.setEndpoint("termserver");
		
		Map<String,Parameter> parameterOverrides = new HashMap<>();
		parameterOverrides.put("Measurement Period", new IntervalParameter( new DateParameter("2019-07-04")
				, true , new DateParameter("2020-07-04"), true));
		
		MeasureContext ctx = new MeasureContext("measureId", parameterOverrides);
		
		MeasureEvaluation evaluation = new MeasureEvaluation();
		evaluation.setDataServerConfig(dataServerConfig);
		evaluation.setTerminologyServerConfig(termServerConfig);
		evaluation.setPatientId("patientId");
		evaluation.setMeasureContext(ctx);
		evaluation.setEvidenceOptions(new MeasureEvidenceOptions(false, MeasureEvidenceOptions.DefineReturnOptions.ALL));
		
		ObjectMapper om = new ObjectMapper();
		String serialized = om.writeValueAsString(evaluation);
		
		MeasureEvaluation deserialized = om.readValue( serialized, MeasureEvaluation.class);
		assertEquals( evaluation, deserialized );
	}
}
