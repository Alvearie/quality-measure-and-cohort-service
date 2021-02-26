/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.cohort.fhir.client.config.FhirServerConfig.LogInfo;

public class MeasureImporterTest extends BaseFhirTest {
	@Test
	public void testImportBundleSuccess200() throws Exception {
		
		String expectedRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/simple_age_measure_v2_2_2-request.json")), StandardCharsets.UTF_8);
		String expectedResponse = new String(Files.readAllBytes(Paths.get("src/test/resources/simple_age_measure_v2_2_2-response.json")), StandardCharsets.UTF_8);
		
		roundTripTest("src/test/resources/simple_age_measure_v2_2_2.zip", expectedRequest, expectedResponse, 200, 0);

	}
	
	@Test
	public void testImportBundleFailure400() throws Exception {
		String expectedRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/simple_age_measure_v2_2_2-request.json")), StandardCharsets.UTF_8);

		OperationOutcome outcome = new OperationOutcome();
		outcome.getText().setDivAsString("<div>Something went wrong</div>");
		String expectedResponse = getFhirParser().encodeResourceToString(outcome);
		
		roundTripTest("src/test/resources/simple_age_measure_v2_2_2.zip", expectedRequest, expectedResponse, 400, 1);
	}
	
	@Test
	public void testImportBundleFailure500() throws Exception {
		String expectedRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/simple_age_measure_v2_2_2-request.json")), StandardCharsets.UTF_8);

		OperationOutcome outcome = new OperationOutcome();
		outcome.getText().setDivAsString("<div>Something went wrong</div>");
		String expectedResponse = getFhirParser().encodeResourceToString(outcome);
		
		roundTripTest("src/test/resources/simple_age_measure_v2_2_2.zip", expectedRequest, expectedResponse, 500, 1);
	}
	
	@Test(expected=ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException.class)
	public void testImportBundleClientConnectFailure() throws Exception {
		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://i.am.not.here");
		
		runTest(fhirConfig, "src/test/resources/simple_age_measure_v2_2_2.zip", -1);
	}
	
	protected String roundTripTest(String inputPath, String expectedRequest, String expectedResponse, int statusCode, int numExpectedErrors) throws Exception {
		String consoleOutput;
	
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Arrays.asList(LogInfo.REQUEST_SUMMARY));
		
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		stubFor(post(urlEqualTo("/")).willReturn(
				aResponse().withStatus(statusCode).withHeader("Content-Type", "application/json")
					.withBody(expectedResponse)));
		
		consoleOutput = runTest(fhirConfig, inputPath, numExpectedErrors);
		
		verify( 1, postRequestedFor(urlEqualTo("/"))
				.withRequestBody(equalTo(expectedRequest)));
		
		return consoleOutput;
	}
	
	protected String runTest(FhirServerConfig fhirConfig, String pathString) throws IOException, JsonProcessingException, Exception {
		return runTest(fhirConfig,pathString,0);
	}

	protected String runTest(FhirServerConfig fhirConfig, String pathString, int expectedNumErrors) throws IOException, JsonProcessingException, Exception {
		String consoleOutput;
		
		Path tmpFile = Files.createTempFile(Paths.get("target"), "fhir-stub", ".json");
		try {
			ObjectMapper om = new ObjectMapper();
			try( Writer w = new FileWriter( tmpFile.toFile() ) ) {
				w.write(om.writeValueAsString(fhirConfig));
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(baos);
			int actualErrors = MeasureImporter.runWithArgs( new String[] { "-m", tmpFile.toString(), "-o", "target", pathString }, out);
			assertEquals( expectedNumErrors, actualErrors );
			
			consoleOutput = baos.toString(StandardCharsets.UTF_8.name());
			
			if( expectedNumErrors == 0 ) { 
				assertTrue( consoleOutput.contains("Process completed with no errors") );
			} else { 
				assertTrue( consoleOutput.contains("Process completed with errors") );
			}
		} finally {
			Files.delete( tmpFile );
		}
		
		return consoleOutput;
	}
}
