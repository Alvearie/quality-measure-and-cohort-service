/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.ibm.cohort.engine.translation.CqlTranslationProvider;
import com.ibm.cohort.engine.translation.InJVMCqlTranslationProvider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class BaseFhirTest {

	static int HTTP_PORT = 0;

	@BeforeClass
	public static void setUpBeforeClass() {
		// get a random local port for test use
		try (ServerSocket socket = new ServerSocket(0)) {
			HTTP_PORT = socket.getLocalPort();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(
			options().port(HTTP_PORT)/* .notifier(new ConsoleNotifier(true)) */);

	protected FhirContext fhirContext = FhirContext.forR4();
	protected IParser fhirParser = fhirContext.newJsonParser().setPrettyPrint(true);

	protected void mockFhirResourceRetrieval(Resource resource) {
		String resourcePath = "/" + resource.getClass().getSimpleName() + "/" + resource.getId();
		mockFhirResourceRetrieval(resourcePath, getFhirParser(), resource);
	}

	protected void mockFhirResourceRetrieval(String resourcePath, Resource resource) {
		mockFhirResourceRetrieval(resourcePath, getFhirParser(), resource);
	}

	protected void mockFhirResourceRetrieval(String resourcePath, IParser encoder, Resource resource) {
		mockFhirResourceRetrieval(resourcePath, getFhirParser(), resource, getFhirServerConfig());
	}

	protected void mockFhirResourceRetrieval(String resourcePath, IParser encoder, Resource resource,
			FhirServerConfig fhirConfig) {
		MappingBuilder builder = get(urlEqualTo(resourcePath));
		mockFhirResourceRetrieval(builder, encoder, resource, fhirConfig);
	}

	protected void mockFhirResourceRetrieval(MappingBuilder builder, Resource resource) {
		mockFhirResourceRetrieval(builder, getFhirParser(), resource, getFhirServerConfig());
	}

	protected void mockFhirResourceRetrieval(MappingBuilder builder, IParser encoder, Resource resource,
			FhirServerConfig fhirConfig) {
		builder = setAuthenticationParameters(fhirConfig, builder);
		stubFor(builder.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
				.withBody(encoder.encodeResourceToString(resource))));
	}

	protected MappingBuilder setAuthenticationParameters(FhirServerConfig fhirConfig, MappingBuilder builder) {
		if (fhirConfig.getUser() != null && fhirConfig.getPassword() != null) {
			builder = builder.withBasicAuth(fhirConfig.getUser(), fhirConfig.getPassword());
		}

		Map<String, String> additionalHeaders = fhirConfig.getAdditionalHeaders();
		if (additionalHeaders != null) {
			for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
				builder = builder.withHeader(header.getKey(), matching(header.getValue()));
			}
		}
		return builder;
	}

	protected IParser getFhirParser() {
		return fhirParser;
	}

	protected FhirServerConfig getFhirServerConfig() {
		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:" + HTTP_PORT);
		return fhirConfig;
	}

	protected Patient getPatient(String id, Enumerations.AdministrativeGender administrativeGender, String birthDateStr)
			throws ParseException {
		Patient patient = new Patient();
		patient.setId(id);
		patient.setGender(administrativeGender);

		if (birthDateStr != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date birthDate = format.parse(birthDateStr);
			patient.setBirthDate(birthDate);
		}
		return patient;
	}

	protected Library getLibrary(String id, String... attachmentData) throws Exception {
		if (attachmentData == null || attachmentData.length == 0
				|| (attachmentData.length > 2) && ((attachmentData.length % 2) != 0)) {
			fail("Invalid attachment data. Data must consist of one or more pairs of resource path and content-type strings");
		}

		List<String> pairs = new ArrayList<String>(Arrays.asList(attachmentData));
		if (pairs.size() == 1) {
			pairs.add("text/cql");
		}

		List<Attachment> attachments = new ArrayList<Attachment>(pairs.size() / 2);
		for (int i = 0; i < pairs.size(); i += 2) {
			String resource = pairs.get(i);
			String contentType = pairs.get(i + 1);

			try (InputStream is = ClassLoader.getSystemResourceAsStream(resource)) {
				assertNotNull(String.format("No such resource %s found in classpath", resource), is);
				String text = IOUtils.toString(is, StandardCharsets.UTF_8);

				Attachment attachment = new Attachment();
				attachment.setContentType(contentType);
				attachment.setData(Base64.encodeBase64(text.getBytes()));
				attachments.add(attachment);
			}
		}

		Library library = new Library();
		library.setId(id);
		library.setName(id);
		// library.setVersion("1.0.0");
		library.setContent(attachments);

		return library;
	}

	public Reference asReference(Resource resource) {
		return new Reference(resource.getIdElement().getResourceType() + "/" + resource.getId());
	}

	public CanonicalType asCanonical(Resource resource) {
		return new CanonicalType(resource.getClass().getSimpleName() + "/" + resource.getId());
	}

	protected CapabilityStatement getCapabilityStatement() {
		CapabilityStatement metadata = new CapabilityStatement();
		metadata.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
		return metadata;
	}

	protected CqlEngineWrapper setupTestFor(Patient patient, String... elm) throws Exception {
		IBMFhirServerConfig fhirConfig = new IBMFhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:" + HTTP_PORT);
		fhirConfig.setUser("fhiruser");
		fhirConfig.setPassword("change-password");
		fhirConfig.setTenantId("default");

		return setupTestFor(patient, fhirConfig, elm);
	}

	protected CqlEngineWrapper setupTestFor(Patient patient, FhirServerConfig fhirConfig, String... resources)
			throws Exception {

		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		mockFhirResourceRetrieval(patient);

		CqlEngineWrapper wrapper = new CqlEngineWrapper();
		if (resources != null) {
			/**
			 * Do some hacking to make the pre-existing test resources still function 
			 * with the updated design.
			 */
			FilenameToVersionedIdentifierStrategy strategy = new DefaultFilenameToVersionedIdentifierStrategy() {
				@Override
				public VersionedIdentifier filenameToVersionedIdentifier(String filename) {
					VersionedIdentifier result = null;
					String basename = FilenameUtils.getBaseName(filename);
					if( basename.startsWith("test") ) {
						result = new VersionedIdentifier().withId("Test").withVersion("1.0.0");
					} else { 
						result = super.filenameToVersionedIdentifier( basename );
					}
					return result;
				}
			};
			
			MultiFormatLibrarySourceProvider sourceProvider = new ClasspathLibrarySourceProvider(
					Arrays.asList(resources),
					strategy);
			CqlTranslationProvider translationProvider = new InJVMCqlTranslationProvider(sourceProvider);

			LibraryLoader libraryLoader = new TranslatingLibraryLoader(sourceProvider, translationProvider);
			wrapper.setLibraryLoader(libraryLoader);
		}

		wrapper.setDataServerConnectionProperties(fhirConfig);
		wrapper.setTerminologyServerConnectionProperties(fhirConfig);
		wrapper.setMeasureServerConnectionProperties(fhirConfig);
		return wrapper;
	}
}
