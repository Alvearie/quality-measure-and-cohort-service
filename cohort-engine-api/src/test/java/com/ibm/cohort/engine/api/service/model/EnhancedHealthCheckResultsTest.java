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
import com.ibm.cohort.engine.api.service.model.FhirServerConnectionStatusInfo.FhirConnectionStatus;
import com.ibm.cohort.engine.api.service.model.FhirServerConnectionStatusInfo.FhirServerConfigType;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class EnhancedHealthCheckResultsTest {

	@Test
	public void when_serialize_deserialize___properties_are_unchanged() throws Exception {

		FhirServerConfig dataServerConfig = new FhirServerConfig();
		dataServerConfig.setEndpoint("dataserver");

		FhirServerConfig termServerConfig = new FhirServerConfig();
		termServerConfig.setEndpoint("termserver");

		EnhancedHealthCheckResults results = new EnhancedHealthCheckResults();
		FhirServerConnectionStatusInfo dataServerConnectionResults = new FhirServerConnectionStatusInfo();
		dataServerConnectionResults.setServerConfigType(FhirServerConfigType.dataServerConfig);
		dataServerConnectionResults.setConnectionResults(FhirConnectionStatus.notAttempted);
		
		FhirServerConnectionStatusInfo terminologyServerConnectionResults = new FhirServerConnectionStatusInfo();
		terminologyServerConnectionResults.setServerConfigType(FhirServerConfigType.terminologyServerConfig);
		terminologyServerConnectionResults.setConnectionResults(FhirConnectionStatus.notAttempted);
		
		results.setDataServerConnectionResults(dataServerConnectionResults);
		results.setTerminologyServerConnectionResults(terminologyServerConnectionResults);

		ObjectMapper om = new ObjectMapper();
		String serialized = om.writeValueAsString(results);

		EnhancedHealthCheckResults deserialized = om.readValue( serialized, EnhancedHealthCheckResults.class);
		assertEquals( results, deserialized );
	}

}