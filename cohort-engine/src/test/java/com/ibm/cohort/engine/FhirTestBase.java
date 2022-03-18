/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Measure.MeasureSupplementalDataComponent;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.cohort.fhir.client.config.FhirServerConfig.LogInfo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class FhirTestBase {

	public static String DEFAULT_RESOURCE_VERSION = "1.0.0";
	
	static int HTTP_PORT = 0;
	static String IBM_PREFIX = "http://ibm.com/fhir/measure";

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
	
	protected IGenericClient newClient() {
		return FhirClientBuilderFactory.newInstance().newFhirClientBuilder(fhirContext)
				.createFhirClient(getFhirServerConfig());
	}

	protected void mockFhirResourceRetrieval(Resource resource) {
		String resourcePath = "/" + resource.getClass().getSimpleName() + "/" + resource.getId() + "?_format=json";
		mockFhirResourceRetrieval(resourcePath, getFhirParser(), resource);
	}

	protected void mockFhirSingletonBundleRetrieval(Resource resource) {
		String resourceType = resource.getClass().getSimpleName();
		String resourcePath = "/" + resourceType + "?url=%2F" + resourceType + "%2F" + resource.getId() + "&_format=json";

		BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();
		bundleEntryComponent.setResource(resource);

		Bundle bundle = new Bundle();
		bundle.addEntry(bundleEntryComponent);
		bundle.setTotal(1);

		mockFhirResourceRetrieval(resourcePath, getFhirParser(), bundle);
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
		mockFhirResourceRetrieval( builder, encoder, resource, fhirConfig, 200);
	}
	
	protected void mockeDelayedFhirResourceRetrieval(MappingBuilder builder, IParser encoder, Resource resource,
													 FhirServerConfig fhirConfig, int statusCode, int delayMillis) {
		String body = null;
		if( resource != null ) {
			body = encoder.encodeResourceToString(resource);
		}

		builder = setAuthenticationParameters(fhirConfig, builder);
		stubFor(builder.willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/json")
										   .withBody(body).withFixedDelay(delayMillis)));
	}
	
	protected void mockFhirResourceRetrieval(MappingBuilder builder, IParser encoder, Resource resource,
			FhirServerConfig fhirConfig, int statusCode) {
		
		String body = null;
		if( resource != null ) {
			body = encoder.encodeResourceToString(resource);
		}
		
		builder = setAuthenticationParameters(fhirConfig, builder);
		stubFor(builder.willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/json")
				.withBody(body)));
	}
	
	protected void mockEmptySearchResults(String fhirType) {
		mockFhirResourceRetrieval( get(urlMatching("/" + fhirType + "\\?.*")), new Bundle() );
	}
	
	protected void mockNotFound(String resource) {
		OperationOutcome outcome = new OperationOutcome();
		outcome.getText().setStatusAsString("generated");
		outcome.getIssueFirstRep().setSeverity(IssueSeverity.ERROR).setCode(OperationOutcome.IssueType.PROCESSING).setDiagnostics(resource);
		
		mockFhirResourceRetrieval( get(urlMatching(resource)), getFhirParser(), outcome, getFhirServerConfig(), 404 );
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
		fhirConfig.setLogInfo(Arrays.asList(LogInfo.REQUEST_SUMMARY));
		return fhirConfig;
	}

	protected Patient mockPatientRetrieval(String id, Enumerations.AdministrativeGender gender, String birthDateStr) 
			throws ParseException {
		Patient patient = getPatient(id, gender, birthDateStr);
		mockFhirResourceRetrieval(patient);
		return patient;
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
	
	protected Patient mockPatientRetrieval(String id, AdministrativeGender gender, int ageInYears) {
		Patient patient = getPatient(id, gender, ageInYears);
		mockFhirResourceRetrieval(patient);
		return patient;
	}

	protected Patient mockPatientRetrievalTimeout(String id, AdministrativeGender gender, int ageInYears, int delayMillis) {
		Patient patient = getPatient(id, gender, ageInYears);
		mockeDelayedFhirResourceRetrieval(
				get(urlEqualTo("/" + patient.getClass().getSimpleName() + "/" + patient.getId() + "?_format=json")),
				getFhirParser(),
				patient,
				getFhirServerConfig(),
				200,
				delayMillis
		);
		return patient;
	}

	protected Patient getPatient(String id, AdministrativeGender gender, int ageInYears) {
		OffsetDateTime birthDate;
		Patient patient = new Patient();
		patient.setId(id);
		patient.setGender(gender);
		
		birthDate = OffsetDateTime.now().minusYears(ageInYears);
		patient.setBirthDate(Date.from(birthDate.toInstant()));
		return patient;
	}	

	protected Library getLibrary(String name, String version, String... attachmentData) throws Exception {
		if (attachmentData == null || attachmentData.length == 0
				|| (attachmentData.length > 2) && ((attachmentData.length % 2) != 0)) {
			fail("Invalid attachment data. Data must consist of one or more pairs of resource path and content-type strings");
		}

		List<String> pairs = new ArrayList<>(Arrays.asList(attachmentData));
		if (pairs.size() == 1) {
			pairs.add("text/cql");
		}

		List<Attachment> attachments = new ArrayList<>();
		for (int i = 0; i < pairs.size(); i += 2) {
			String resource = pairs.get(i);
			String contentType = pairs.get(i + 1);

			try (InputStream is = ClassLoader.getSystemResourceAsStream(resource)) {
				assertNotNull(String.format("No such resource %s found in classpath", resource), is);
				String text = IOUtils.toString(is, StandardCharsets.UTF_8);

				Attachment attachment = new Attachment();
				attachment.setContentType(contentType);
				attachment.setData(text.getBytes());
				attachments.add(attachment);
			}
		}
		
		Library library = new Library();
		library.setId("library-" + name + "-" + version);
		library.setName(name);
		library.setUrl(IBM_PREFIX + "/Library/" + name);
		library.setVersion(version);
		library.setContent(attachments);

		return library;
	}

	public Reference asReference(Resource resource) {
		return new Reference(resource.getIdElement().getResourceType() + "/" + resource.getId());
	}

	public CanonicalType asCanonical(MetadataResource resource) {
		//return new CanonicalType(resource.getClass().getSimpleName() + "/" + resource.getId());
		String canonical = "http://ibm.com/fhir/measure/" + resource.getClass().getSimpleName() + "/" + resource.getName();
		if( resource.getVersion() != null ) {
			canonical = canonical + "|" + resource.getVersion();
		}
		return new CanonicalType(canonical);
	}

	protected CapabilityStatement getCapabilityStatement() {
		CapabilityStatement metadata = new CapabilityStatement();
		metadata.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
		return metadata;
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
	
	protected Bundle makeBundle(List<? extends Resource> resources) {
		return makeBundle( resources.toArray(new Resource[resources.size()]));
	}
	
	protected Bundle makeBundle(Resource... resources) {		
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.SEARCHSET);
		bundle.setTotal(resources != null ? resources.length : 0);
		if( resources != null ) {
			for (Resource l : resources) {
				bundle.addEntry().setResource(l).setFullUrl("/" + l.getIdBase() + "/" + l.getId());
			}
		}
		return bundle;
	}
	
	protected File createFhirConfigFile() throws IOException, JsonProcessingException {
		return createFhirConfigFile("target/fhir-stub.json");
	}
	
	protected File createFhirConfigFile(String path) throws IOException, JsonProcessingException {
		File tmpFile = new File(path);
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}
		return tmpFile;
	}
	
	protected ValueSet mockValueSetRetrieval(String id, String system, String code) throws UnsupportedEncodingException {
		return mockValueSetRetrieval(id, null, system, code);
	}

	protected ValueSet mockValueSetRetrieval(String id, String version, String system, String code) throws UnsupportedEncodingException {
		String theID = null;
		String theURL = null;
		if( id.startsWith("urn:oid:") ) {
			theURL = id;
			theID = id.replace("urn:oid:", "");
		} else if( id.startsWith("http") ) {
			theURL = id;
			theID = id.substring(id.lastIndexOf("/") + 1);
		} else { 
			theURL = "/ValueSet/" + theID;
			theID = id;
		}
		
		ValueSet resource = new ValueSet();
		resource.setId(theID);
		resource.setVersion(version);
		resource.setUrl(theURL);
		resource.getCompose().getIncludeFirstRep().setSystem(system).getConceptFirstRep().setCode(code);
		resource.getExpansion().getContainsFirstRep().setSystem(system).setCode(code);
		mockFhirResourceRetrieval(resource);
		
		// When you specify the VS as the right hand side of a retrieve (e.g. [Condition : "ValueSet"])
		// the urn:oid prefix is stripped off
		mockFhirResourceRetrieval("/ValueSet?url=" + theID + "&_format=json", makeBundle(resource));
		
		// When you specify the VS as the right hand side of an "in" expression (e.g. Condition.code in "ValueSet")
		// the urn:oid prefix is preserved
		String url = URLEncoder.encode( resource.getUrl(), StandardCharsets.UTF_8.toString() );
		mockFhirResourceRetrieval("/ValueSet?url=" + url + "&_format=json", makeBundle(resource));
		if( version != null ) {
			mockFhirResourceRetrieval("/ValueSet?url=" + url + "&version=" + resource.getVersion() + "&_format=json", makeBundle(resource));
		}
		
		String expandUrl = "/ValueSet/" + theID + "/$expand?_format=json";
		mockFhirResourceRetrieval(expandUrl, resource);
		mockFhirResourceRetrieval(post(urlEqualTo(expandUrl)), resource);
		
		Parameters response = new Parameters();
		response.addParameter().setValue(new BooleanType(true));
		
		String systemUrl = URLEncoder.encode( system, StandardCharsets.UTF_8.toString() );
		mockFhirResourceRetrieval("/ValueSet/" + theID + "/$validate-code?code=" + code + "&system=" + systemUrl + "&_format=json", response);

		return resource;
	}

	protected MeasureSupplementalDataComponent createSupplementalDataComponent(String defineName, String text) {
		MeasureSupplementalDataComponent supplementalDataComponent = new MeasureSupplementalDataComponent();
		CodeableConcept supplementalCC = new CodeableConcept();
		Coding supplementalCoding = new Coding();
		supplementalCoding.setCode("supplemental-data-coding");
		supplementalCC.setCoding(Arrays.asList(supplementalCoding));
		supplementalCC.setText(text);
		supplementalDataComponent.setCode(supplementalCC);
		
		CodeableConcept usage = new CodeableConcept();
		Coding usageCoding = new Coding();
		usageCoding.setCode("supplemental-data");
		usage.setCoding(Arrays.asList(usageCoding));
		supplementalDataComponent.setUsage(Arrays.asList(usage));
		
		Expression supplementalExpression = new Expression();
		supplementalExpression.setExpression(defineName);
		supplementalExpression.setLanguage("text/cql.identifier");
		
		supplementalDataComponent.setCriteria(supplementalExpression);
		
		return supplementalDataComponent;
	}
}
