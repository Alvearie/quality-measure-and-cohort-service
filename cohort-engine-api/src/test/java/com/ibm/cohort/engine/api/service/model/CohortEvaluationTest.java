/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.engine.api.service.model;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.cql.evaluation.CqlDebug;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.evaluation.parameters.DateParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntervalParameter;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class CohortEvaluationTest {

	@Test
	public void when_serialize_deserialize___properties_are_unchanged() throws Exception {

		FhirServerConfig dataServerConfig = new FhirServerConfig();
		dataServerConfig.setEndpoint("dataserver");

		FhirServerConfig termServerConfig = new FhirServerConfig();
		termServerConfig.setEndpoint("termserver");

		Map<String, Parameter> parameterOverrides = new HashMap<>();
		parameterOverrides.put("Measurement Period", new IntervalParameter( new DateParameter("2019-07-04")
				, true , new DateParameter("2020-07-04"), true));



		CohortEvaluation evaluation = new CohortEvaluation();
		evaluation.setDataServerConfig(dataServerConfig);
		evaluation.setTerminologyServerConfig(termServerConfig);
		evaluation.setPatientIds("patientId");
		evaluation.setParameters(parameterOverrides);
		evaluation.setLoggingLevel(CqlDebug.TRACE);
		evaluation.setEntrypoint("test-1.0.0.cql");
		evaluation.setDefineToRun("fakeDefine");

		ObjectMapper om = new ObjectMapper();
		String serialized = om.writeValueAsString(evaluation);

		CohortEvaluation deserialized = om.readValue( serialized, CohortEvaluation.class);
		assertEquals( evaluation, deserialized );
	}

}