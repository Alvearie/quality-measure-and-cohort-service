/*
 *
 *  * (C) Copyright IBM Corp. 2022
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.engine.api.service.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class EnhancedHealthCheckInputTest {

	@Test
	public void when_serialize_deserialize___properties_are_unchanged() throws Exception {

		FhirServerConfig dataServerConfig = new FhirServerConfig();
		dataServerConfig.setEndpoint("dataserver");

		FhirServerConfig termServerConfig = new FhirServerConfig();
		termServerConfig.setEndpoint("termserver");

		EnhancedHealthCheckInput input = new EnhancedHealthCheckInput();
		input.setDataServerConfig(dataServerConfig);
		input.setTerminologyServerConfig(termServerConfig);

		ObjectMapper om = new ObjectMapper();
		String serialized = om.writeValueAsString(input);

		EnhancedHealthCheckInput deserialized = om.readValue( serialized, EnhancedHealthCheckInput.class);
		assertEquals( input, deserialized );
	}

}