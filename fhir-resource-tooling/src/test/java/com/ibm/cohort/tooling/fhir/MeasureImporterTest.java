/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.cohort.fhir.client.config.FhirServerConfig.LogInfo;

public class MeasureImporterTest extends BaseFhirTest {
	@Test
	public void testImportAllNewResources() throws Exception {
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Arrays.asList(LogInfo.REQUEST_SUMMARY));
		
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Bundle noResults = new Bundle();
		
		mockFhirResourceRetrieval("/Library?url=Library%2Flibrary-CovidConfirmedOverSuspected_genCql", noResults);
		mockFhirResourceRetrieval("/Library?url=Library%2Flibrary-SuspectedCovid", noResults);
		mockFhirResourceRetrieval("/Library?url=Library%2Flibrary-GotCovid", noResults);
		mockFhirResourceRetrieval("/Library?url=Library%2Flibrary-FHIRHelpers", noResults);
		mockFhirResourceRetrieval("/Measure?url=Measure%2Fmeasure-CovidConfirmedOverSuspected", noResults);
		
		String libraryId = UUID.randomUUID().toString();
		
		mockFhirResourcePost("/Library", libraryId, "1");
		
		mockFhirResourcePut("/Library/" + libraryId, "2");
		
		String measureId = UUID.randomUUID().toString();
		
		mockFhirResourcePost("/Measure", measureId, "1");
		
		mockFhirResourcePut("/Measure/" + measureId, "2");
		
		runTest(fhirConfig, "src/test/resources/covid_confirmed_over_suspected_v1_1_1.zip");
		
		verify( 4, postRequestedFor(urlEqualTo("/Library")) );
		verify( 1, postRequestedFor(urlEqualTo("/Measure")) );
		verify( 1, putRequestedFor(urlEqualTo("/Library/" + libraryId)) );
		verify( 1, putRequestedFor(urlEqualTo("/Measure/" + measureId)) );	
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
	
	protected String getLocation(String localUrl, String version) {
		return getFhirServerConfig().getEndpoint() + localUrl + "/_history/" + version;
	}
	
	protected void mockFhirResourcePut(String localUrl, String newVersion) {
		stubFor(put(urlEqualTo(localUrl)).willReturn(
				aResponse().withStatus(200)
					.withHeader("Location", getLocation(localUrl, newVersion))
					.withHeader("ETag", newVersion)
					.withHeader("Last-Modified", "2021-01-12T21:21:21.286Z")) );
	}
	
	@Test
	public void testImportAllExistingResources() throws Exception {
		String libraryId = null;
		String measureId = null;
		
		FhirServerConfig fhirConfig = getFhirServerConfig();
		fhirConfig.setLogInfo(Arrays.asList(LogInfo.REQUEST_SUMMARY));
		
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		try( InputStream is = new FileInputStream("src/test/resources/covid_confirmed_over_suspected_v1_1_1.zip") ) {
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (MeasureImporter.isDeployable(entry)) {
					String id = UUID.randomUUID().toString();
					
					MetadataResource resource = (MetadataResource) getFhirParser().parseResource(zis);
					resource.setId( id );
					
					String searchUrl = "/" + resource.fhirType() + "?url=" + URLEncoder.encode(resource.getUrl(), "UTF-8");
					
					Bundle.BundleEntryComponent bundleEntry = new Bundle.BundleEntryComponent();
					bundleEntry.setResource(resource);
					
					Bundle bundle = new Bundle();
					bundle.addEntry(bundleEntry);
					
					mockFhirResourceRetrieval( searchUrl, bundle );
					
					String localUrl = "/" + resource.fhirType() + "/" + id;
					String fullUrl = fhirConfig.getEndpoint() + localUrl;
					
					stubFor(put(urlEqualTo(localUrl)).willReturn(
							aResponse().withStatus(200)
								.withHeader("Location", fullUrl)
								.withHeader("ETag", /*version=*/"2") 
								.withHeader("Last-Modified", "2021-01-12T21:21:21.286Z")) );
					
					if( resource.getResourceType() == ResourceType.Measure ) { 
						measureId = id;
					} else if( resource.getUrl().equals("Library/library-CovidConfirmedOverSuspected_genCql") ) {
						libraryId = id;
					}
				}
			}
		}
		
		// sanity check the results of the ZIP loader
		assertNotNull(libraryId);
		assertNotNull(measureId);
		
		runTest(fhirConfig, "src/test/resources/covid_confirmed_over_suspected_v1_1_1.zip");
		
		verify( 1, putRequestedFor(urlEqualTo("/Library/" + libraryId)) );
		verify( 1, putRequestedFor(urlEqualTo("/Measure/" + measureId)) );
	}

	protected void runTest(FhirServerConfig fhirConfig, String pathString) throws IOException, JsonProcessingException, Exception {
		Path tmpFile = Files.createTempFile(Paths.get("target"), "fhir-stub", ".json");
		try {
			ObjectMapper om = new ObjectMapper();
			try( Writer w = new FileWriter( tmpFile.toFile() ) ) {
				w.write(om.writeValueAsString(fhirConfig));
			}
			
			OutputStream baos = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(baos);
			MeasureImporter.runWithArgs( new String[] { "-m", tmpFile.toString(), pathString }, out);
		} finally {
			Files.delete( tmpFile );
		}
	}
}
