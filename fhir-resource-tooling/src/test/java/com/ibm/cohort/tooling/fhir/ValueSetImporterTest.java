/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class ValueSetImporterTest extends BaseFhirTest {

	private final String defaultInputFile = "src/test/resources/2.16.840.1.113762.1.4.1114.7.xlsx";
	private final String valueSetIdentifier = "2.16.840.1.113762.1.4.1114.7";
	private final String resourcePath = "/ValueSet?url=" + URLEncoder.encode("http://cts.nlm.nih.gov/fhir/ValueSet/") + valueSetIdentifier + "&_format=json";

	public class TestImporter extends ValueSetImporter{
		final AmazonS3 client;
		TestImporter(AmazonS3 mockClient){
			client = mockClient;
		}

		@Override
		public AmazonS3 createClient(String api_key, String service_instance_id, String endpoint_url, String location){
			return client;
		}
	}

	@Test
	public void testImportAllNewResources() throws IOException {
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Collections.singletonList(FhirServerConfig.LogInfo.REQUEST_SUMMARY));

		mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

		String valueSetId = UUID.randomUUID().toString();
		Bundle noResults = new Bundle();

		mockFhirResourceRetrieval(resourcePath, noResults);

		mockFhirResourcePost("/ValueSet?_format=json", valueSetId, "1");

		runTest(fhirConfig, defaultInputFile);

		verify( 1, postRequestedFor(urlEqualTo("/ValueSet?_format=json")) );
	}

	@Test
	public void testImportAllNewResourcesWithComments() throws IOException {
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Collections.singletonList(FhirServerConfig.LogInfo.REQUEST_SUMMARY));

		mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

		String valueSetId = UUID.randomUUID().toString();
		Bundle noResults = new Bundle();
		mockFhirResourceRetrieval(resourcePath, noResults);

		mockFhirResourcePost("/ValueSet?_format=json", valueSetId, "1");

		String inputFile = "src/test/resources/2.16.840.1.113762.1.4.1114.7-Comments.xlsx";
		runTest(fhirConfig, inputFile);

		verify( 1, postRequestedFor(urlEqualTo("/ValueSet?_format=json")) );
	}

	@Test
	public void testImportAllExistingResources() throws IOException {
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Collections.singletonList(FhirServerConfig.LogInfo.REQUEST_SUMMARY));

		mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());

		Bundle oneResult = new Bundle();
		Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
		ValueSet set = new ValueSet();
		String url = "http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1114.7";
		set.setUrl(url);
		entryComponent.setFullUrl(url);
		entryComponent.setResource(set);
		oneResult.addEntry(entryComponent);
		mockFhirResourceRetrieval(resourcePath, oneResult);

		runTest(fhirConfig, defaultInputFile);

		verify( 1, getRequestedFor(urlEqualTo(resourcePath)) );
		verify( exactly(0), postRequestedFor(urlEqualTo("/ValueSet?_format=json")) );
	}

	@Test
	public void testCorrectMethodHit() throws IOException {
		String inputFile = "src/test/resources/2.16.840.1.113762.1.4.1114.7-Comments.xlsx";

		AmazonS3 client = Mockito.mock(AmazonS3.class);
		when(client.putObject(Mockito.any())).thenReturn(null);

		try {
			OutputStream baos = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(baos);
			TestImporter importer = new TestImporter(client);
			importer.runWithArgs( new String[] { inputFile, "--output-locations", "S3", "--bucket", "kwasny-test", "--s3-configuration", "src/test/resources/example-cos-credentials.json"}, out);
			Mockito.verify(client).putObject(any());
		} finally {
		}
	}

	@Test
	public void testSaveToJSON() throws Exception {
		runSaveToFileTest("2.16.840.1.113762.1.4.1114.7", ".xlsx", "json");
	}
	
	@Test
	public void testSaveToXML() throws Exception {		
		runSaveToFileTest("2.16.840.1.113762.1.4.1114.7", ".xlsx", "xml");
	}
	
	@Test(expected=com.beust.jcommander.ParameterException.class)
	public void testBadOutputFormat() throws Exception {		
		runSaveToFileTest("2.16.840.1.113762.1.4.1114.7", ".xlsx", "garbage");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBadServerAndFileSystemParameters() throws Exception {
		ValueSetImporter.main(new String[] { "-p", Paths.get("target").toString(), "-o", "json", "-m", "someJunkServerName",
				"src/test/resources/" + "2.16.840.1.113762.1.4.1114.7.xlsx", "--output-locations", "LOCAL" });
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNoServerOrFileSystemParameters() throws Exception {
		ValueSetImporter.main(new String[] {"src/test/resources/" + "2.16.840.1.113762.1.4.1114.7.xlsx" });
	}
	
	

	protected void mockFhirResourcePost(String localUrl, String newId, String newVersion) {
		stubFor(post(urlEqualTo(localUrl)).willReturn(
				aResponse().withStatus(201)
						.withHeader("Location", getLocation(localUrl, newId, newVersion))
						.withHeader("ETag", newVersion)
						.withHeader("Last-Modified", "2021-01-12T21:21:21.286Z")) );
	}

	protected String getLocation(String localUrl, String newId, String version) {
		return getFhirServerConfig().getEndpoint() + localUrl + "/" + newId + "/_history/" + version;
	}

	private void runTest(FhirServerConfig fhirConfig, String pathString) throws IOException {
		Path tmpFile = Files.createTempFile(Paths.get("target"), "fhir-stub", ".json");
		try {
			ObjectMapper om = new ObjectMapper();
			try( Writer w = new FileWriter( tmpFile.toFile() ) ) {
				w.write(om.writeValueAsString(fhirConfig));
			}

			OutputStream baos = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(baos);
			ValueSetImporter importer = new ValueSetImporter();
			importer.runWithArgs( new String[] { "-m", tmpFile.toString(), pathString }, out);
		} finally {
			Files.delete( tmpFile );
		}
	}
	
	private void runSaveToFileTest(String inputSpreadSheet, String inputSpreadSheetExt, String inputFormat) throws Exception {
		String inputSpreadSheetFileName = inputSpreadSheet + inputSpreadSheetExt;
		String outputFile = Paths.get("target").toString() + "/" + inputSpreadSheet + "." + inputFormat;
		try {
			ValueSetImporter.main(new String[] { "-p", Paths.get("target").toString(), "-o", inputFormat,
					"src/test/resources/" + inputSpreadSheetFileName, "--output-locations", "LOCAL" });

			assertTrue(Files.exists(Paths.get(outputFile)));
		} finally {
			if (Files.exists(Paths.get(outputFile))) {
				Files.delete(Paths.get(outputFile));
			}
		}
	}
}