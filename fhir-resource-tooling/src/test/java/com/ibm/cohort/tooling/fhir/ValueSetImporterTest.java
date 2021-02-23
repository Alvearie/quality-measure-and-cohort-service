package com.ibm.cohort.tooling.fhir;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

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
import java.util.Arrays;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class ValueSetImporterTest extends BaseFhirTest {

	private final String defaultInputFile = "src/test/resources/2.16.840.1.113762.1.4.1114.7.xlsx";
	private final String valueSetIdentifier = "2.16.840.1.113762.1.4.1114.7";
	private final String resourcePath = "/ValueSet?url=" + URLEncoder.encode("http://cts.nlm.nih.gov/fhir/ValueSet/") + valueSetIdentifier;
	@Test
	public void testImportAllNewResources() throws IOException {
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Arrays.asList(FhirServerConfig.LogInfo.REQUEST_SUMMARY));

		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		String valueSetId = UUID.randomUUID().toString();
		Bundle noResults = new Bundle();

		mockFhirResourceRetrieval(resourcePath, noResults);

		mockFhirResourcePost("/ValueSet", valueSetId, "1");

		runTest(fhirConfig, defaultInputFile);

		verify( 1, postRequestedFor(urlEqualTo("/ValueSet")) );
	}

	@Test
	public void testImportAllNewResourcesWithComments() throws IOException {
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Arrays.asList(FhirServerConfig.LogInfo.REQUEST_SUMMARY));

		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		String valueSetId = UUID.randomUUID().toString();
		Bundle noResults = new Bundle();
		mockFhirResourceRetrieval(resourcePath, noResults);

		mockFhirResourcePost("/ValueSet", valueSetId, "1");

		String inputFile = "src/test/resources/2.16.840.1.113762.1.4.1114.7-Comments.xlsx";
		runTest(fhirConfig, inputFile);

		verify( 1, postRequestedFor(urlEqualTo("/ValueSet")) );
	}

	@Test
	public void testImportAllExistingResources() throws IOException {
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Arrays.asList(FhirServerConfig.LogInfo.REQUEST_SUMMARY));

		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		Bundle oneResult = new Bundle();
		Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
		ValueSet set = new ValueSet();
		set.setUrl("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1114.7");
		entryComponent.setResource(set);
		oneResult.addEntry(entryComponent);
		mockFhirResourceRetrieval(resourcePath, oneResult);

		runTest(fhirConfig, defaultInputFile);

		verify( 1, getRequestedFor(urlEqualTo(resourcePath)) );
		verify( exactly(0), postRequestedFor(urlEqualTo("/ValueSet")) );
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

	protected void runTest(FhirServerConfig fhirConfig, String pathString) throws IOException {
		Path tmpFile = Files.createTempFile(Paths.get("target"), "fhir-stub", ".json");
		try {
			ObjectMapper om = new ObjectMapper();
			try( Writer w = new FileWriter( tmpFile.toFile() ) ) {
				w.write(om.writeValueAsString(fhirConfig));
			}

			OutputStream baos = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(baos);
			ValueSetImporter.runWithArgs( new String[] { "-m", tmpFile.toString(), pathString }, out);
		} finally {
			Files.delete( tmpFile );
		}
	}
}