/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Time;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class CqlEngineWrapperTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(options().port(8089)/* .notifier(new ConsoleNotifier(true)) */);

	@Test
	public void testPatientIsFemaleTrue() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});

		assertEquals(1, count.get());
	}

	@Test
	public void testPatientIsFemaleFalse() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.MALE, null);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("Female", expression);
					assertEquals(Boolean.FALSE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testCorrectLibraryVersionSpecified() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.MALE, null);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", /* parameters= */null, new HashSet<>(Arrays.asList("Female")),
				Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("Female", expression);
					assertEquals(Boolean.FALSE, result);
				});
		assertEquals(1, count.get());
	}

	@Test(expected = Exception.class)
	@Ignore
	// TODO: Restore when InMemoryLibraryLoader or whatever becomes production use
	// becomes version aware
	public void testIncorrectLibraryVersionSpecified() throws Exception {

		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.MALE);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "9.9.9", /* parameters= */null, new HashSet<>(Arrays.asList("Female")),
				Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.FALSE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testRequiredCQLParameterSpecifiedPatientOutOfRange() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("MaxAge", 40);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", parameters, new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.FALSE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testRequiredCQLParameterSpecifiedPatientInRange() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("MaxAge", 50);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", parameters, new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test(expected = Exception.class)
	public void testMissingRequiredCQLParameterNoneSpecified() throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// At the reference date specified in the CQL definition, the
		// Patient will be 30 years old.
		Date birthDate = format.parse("2000-08-01");

		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		patient.setBirthDate(birthDate);

		Map<String, Object> parameters = null;

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", parameters, new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test(expected = Exception.class)
	public void testMissingRequiredCQLParameterSomeSpecified() throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// At the reference date specified in the CQL definition, the
		// Patient will be 30 years old.
		Date birthDate = format.parse("2000-08-01");

		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		patient.setBirthDate(birthDate);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("Unused", 100);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", parameters, new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testSimplestHTTPRequestSettings() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", /* parameters= */null, new HashSet<>(Arrays.asList("Female")),
				Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	@Ignore // TODO: Figure out the ConceptRef evaluate not supported message
	public void testConditionClinicalStatusActiveIsMatched() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Condition condition = new Condition();
		condition.setId("condition");
		condition.setSubject(new Reference("Patient/123"));
		condition
				.setClinicalStatus(new CodeableConcept()
						.addCoding(new Coding().setCode("active")
								.setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical"))
						.setText("Active"));

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", getFhirParser(), condition, fhirConfig);

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig, "cql/condition/FHIRHelpers.xml",
				"cql/condition/test-status-active.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateWithEngineWrapper("Test", "1.0.0", /* parameters= */null,
				new HashSet<>(Arrays.asList("HasActiveCondition")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("HasActiveCondition", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testMainWithParams() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		setupTestFor(patient, fhirConfig, "cql/basic/test.xml");

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(fhirConfig));
		}

		try {
			PrintStream originalOut = System.out;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (PrintStream captureOut = new PrintStream(baos)) {
				System.setOut(captureOut);
				CqlEngineWrapper.main(new String[] { "-d", tmpFile.getAbsolutePath(), "-t", tmpFile.getAbsolutePath(),
						"-f", "src/test/resources/cql/parameters", "-l", "Test", "-e", "Female", "-e", "Male", "-c",
						"123", "-p", "MaxAge:integer:40" });
			} finally {
				System.setOut(originalOut);
			}

			String output = new String(baos.toByteArray());
			String[] lines = output.split("\r?\n");
			assertEquals(3, lines.length);

			verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
		} finally {
			tmpFile.delete();
		}
	}

	@Test
	public void testMainNoParams() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		setupTestFor(patient, fhirConfig, "cql/basic/test.xml");

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(fhirConfig));
		}

		try {
			PrintStream originalOut = System.out;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (PrintStream captureOut = new PrintStream(baos)) {
				System.setOut(captureOut);
				CqlEngineWrapper.main(new String[] { "-d", tmpFile.getAbsolutePath(), "-t", tmpFile.getAbsolutePath(),
						"-f", "src/test/resources/cql/basic", "-l", "Test", "-e", "Female", "-e", "Male", "-e",
						"Over the hill", "-c", "123" });
			} finally {
				System.setOut(originalOut);
			}

			String output = new String(baos.toByteArray());
			String[] lines = output.split("\r?\n");
			assertEquals(4, lines.length);

			verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
		} finally {
			tmpFile.delete();
		}
	}

	@Test
	public void testMainZippedLibraries() throws Exception {

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		IParser encoder = getFhirParser();

		mockFhirResourceRetrieval("/metadata", encoder, getCapabilityStatement(), fhirConfig);

		Patient justRight = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");
		mockFhirResourceRetrieval("/Patient/" + justRight.getId(), encoder, justRight, fhirConfig);

		Patient tooOld = getPatient("456", Enumerations.AdministrativeGender.FEMALE, "1900-08-01");
		mockFhirResourceRetrieval("/Patient/" + tooOld.getId(), encoder, tooOld, fhirConfig);

		Patient tooManly = getPatient("789", Enumerations.AdministrativeGender.MALE, "1978-08-01");
		mockFhirResourceRetrieval("/Patient/" + tooManly.getId(), encoder, tooManly, fhirConfig);

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(fhirConfig));
		}

		try {
			PrintStream originalOut = System.out;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (PrintStream captureOut = new PrintStream(baos)) {
				System.setOut(captureOut);
				CqlEngineWrapper.main(new String[] { "-d", tmpFile.getAbsolutePath(), "-t", tmpFile.getAbsolutePath(),
						"-f", "src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip", "-l",
						"Breast-Cancer-Screening", "-v", "1.0.0", "-e", "Female", "-e", "40-65 years of age", "-e",
						"MeetsInclusionCriteria", "-c", "123", "-c", "456", "-c", "789" });
			} finally {
				System.setOut(originalOut);
			}

			String output = new String(baos.toByteArray());
			String[] lines = output.split("\r?\n");

			assertEquals(output, 10, lines.length);
			System.out.println(output);

			verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
			verify(1, getRequestedFor(urlEqualTo("/Patient/456")));
			verify(1, getRequestedFor(urlEqualTo("/Patient/789")));
		} finally {
			tmpFile.delete();
		}
	}

	@Test
	public void testMainZippedLibrariesWithCompilation() throws Exception {

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		IParser encoder = getFhirParser();

		mockFhirResourceRetrieval("/metadata", encoder, getCapabilityStatement(), fhirConfig);

		Patient justRight = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");
		mockFhirResourceRetrieval("/Patient/" + justRight.getId(), encoder, justRight, fhirConfig);

		Patient tooOld = getPatient("456", Enumerations.AdministrativeGender.FEMALE, "1900-08-01");
		mockFhirResourceRetrieval("/Patient/" + tooOld.getId(), encoder, tooOld, fhirConfig);

		Patient tooManly = getPatient("789", Enumerations.AdministrativeGender.MALE, "1978-08-01");
		mockFhirResourceRetrieval("/Patient/" + tooManly.getId(), encoder, tooManly, fhirConfig);

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(fhirConfig));
		}

		try {
			PrintStream originalOut = System.out;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (PrintStream captureOut = new PrintStream(baos)) {
				System.setOut(captureOut);
				CqlEngineWrapper.main(new String[] { "-d", tmpFile.getAbsolutePath(), "-t", tmpFile.getAbsolutePath(),
						"-f", "src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip", "-l",
						"Breast-Cancer-Screening", "-v", "1.0.0", "-e", "Female", "-e", "40-65 years of age", "-e",
						"MeetsInclusionCriteria", "-c", "123", "-c", "456", "-c", "789", "-s", "CQL" });
			} finally {
				System.setOut(originalOut);
			}

			String output = new String(baos.toByteArray());
			String[] lines = output.split("\r?\n");

			assertEquals(output, 10, lines.length);
			System.out.println(output);

			verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
			verify(1, getRequestedFor(urlEqualTo("/Patient/456")));
			verify(1, getRequestedFor(urlEqualTo("/Patient/789")));
		} finally {
			tmpFile.delete();
		}
	}

	@Test
	public void testMainFHIRLibrariesWithDependencies() throws Exception {

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");

		IParser encoder = getFhirParser();

		mockFhirResourceRetrieval("/metadata", encoder, getCapabilityStatement(), fhirConfig);

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");
		mockFhirResourceRetrieval("/Patient/" + patient.getId(), encoder, patient, fhirConfig);

		Library root = getLibrary("Breast-Cancer-Screening", "cql/includes/Breast-Cancer-Screening.cql");
		Library helpers = getLibrary("FHIRHelpers", "cql/includes/FHIRHelpers.cql", "text/cql",
				"cql/includes/FHIRHelpers.xml", "application/elm+json");

		RelatedArtifact related = new RelatedArtifact();
		related.setType(RelatedArtifactType.DEPENDSON);
		related.setResource("/Library/" + helpers.getId());
		root.addRelatedArtifact(related);

		mockFhirResourceRetrieval("/Library/" + root.getId(), encoder, root, fhirConfig);
		mockFhirResourceRetrieval("/Library/" + helpers.getId(), encoder, helpers, fhirConfig);

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(fhirConfig));
		}

		try {
			PrintStream originalOut = System.out;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (PrintStream captureOut = new PrintStream(baos)) {
				System.setOut(captureOut);
				CqlEngineWrapper.main(new String[] { "-d", tmpFile.getAbsolutePath(), "-f", root.getId(), "-l",
						root.getId(), "-v", "1", "-c", patient.getId(), "-s", "CQL" });
			} finally {
				System.setOut(originalOut);
			}

			String output = new String(baos.toByteArray());
			String[] lines = output.split("\r?\n");

			assertEquals(output, 11, lines.length);
			System.out.println(output);

			verify(1, getRequestedFor(urlEqualTo("/Patient/" + patient.getId())));
			verify(1, getRequestedFor(urlEqualTo("/Library/" + root.getId())));
			verify(1, getRequestedFor(urlEqualTo("/Library/" + helpers.getId())));
		} finally {
			tmpFile.delete();
		}
	}

	protected IParser getFhirParser() {
		FhirContext fhirContext = FhirContext.forR4();
		IParser encoder = fhirContext.newJsonParser().setPrettyPrint(true);
		return encoder;
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
		library.setContent(attachments);

		return library;
	}

	@Test
	public void testNumCallsUsingEngineWrapperMethod() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateWithEngineWrapper("Test", null, /* parameters= */null, null, Arrays.asList("123"),
				(p, e, r) -> {
					count.incrementAndGet();
					System.out.println("Expression: " + e);
					System.out.println("Result: " + r);
				});
		assertEquals(4, count.get());
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test
	public void testNumCallsUsingPerDefineMethod() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateExpressionByExpression("Test", null, /* parameters= */null, null, Arrays.asList("123"),
				(p, e, r) -> {
					count.incrementAndGet();
					System.out.println("Expression: " + e);
					System.out.println("Result: " + r);
				});
		assertEquals(4, count.get());
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test
	// @Ignore // This isn't working right now due to weirdness in the CqlEngine
	public void testNumCallsWithParamsUsingEngineWrapperMethod() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("MaxAge", 40);

		final AtomicBoolean found = new AtomicBoolean(false);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateWithEngineWrapper("Test", null, parameters, null, Arrays.asList("123"), (p, e, r) -> {
			count.incrementAndGet();
			if (e.equals("ParamMaxAge")) {
				assertEquals("Unexpected value for expression result", "40", r);
				found.set(true);
			}
		});
		assertEquals("Missing expression result", true, found.get());

		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test
	public void testNumCallsWithParamsUsingPerDefineMethod() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("MaxAge", 40);

		final AtomicBoolean found = new AtomicBoolean(false);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateExpressionByExpression("Test", null, parameters, null, Arrays.asList("123"), (p, e, r) -> {
			count.incrementAndGet();
			if (e.equals("ParamMaxAge")) {
				assertEquals("Unexpected value for expression result", "40", r);
				found.set(true);
			}
		});
		assertEquals("Missing expression result", true, found.get());
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test
	@Ignore // uncomment when JSON starts working -
			// https://github.com/DBCG/cql_engine/issues/405
	public void testJsonCQLWithIncludes() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-08-01");

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/includes/Breast-Cancer-Screening.json",
				"cql/includes/FHIRHelpers.json");

		final AtomicBoolean found = new AtomicBoolean(false);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Breast-Cancer-Screening", "1", /* parameters= */null, null, Arrays.asList("123"),
				(p, e, r) -> {
					count.incrementAndGet();
					if (e.equals("MeetsInclusionCriteria")) {
						assertEquals("Unexpected value for expression result", Boolean.TRUE, r);
						found.set(true);
					}
				});
		assertEquals("Missing expression result", true, found.get());
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidWrapperSetup() throws Exception {
		CqlEngineWrapper wrapper = new CqlEngineWrapper();
		wrapper.evaluate("Test", null, null, null, Arrays.asList("123"), (p, e, r) -> {
			/* do nothing */ });
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingRequiredInputParameters() throws Exception {
		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");
		wrapper.evaluate(null, null, null, null, null, null);
	}

	@Test(expected = Exception.class)
	public void testCannotConnectToFHIRDataServer() throws Exception {
		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://its.not.me");

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig, "cql/basic/test.xml");
		wrapper.evaluate("Test", /* version= */null, /* parameters= */null, /* expressions= */null,
				Arrays.asList("123"), (p, e, r) -> {
					fail("Execution should not reach here");
				});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidLibraryName() throws Exception {
		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://its.not.me");

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig, "cql/basic/test.xml");
		wrapper.evaluate("NotCorrect", /* version= */null, /* parameters= */null, /* expressions= */null,
				Arrays.asList("123"), (p, e, r) -> {
					fail("Execution should not reach here");
				});
	}

	private CqlEngineWrapper setupTestFor(Patient patient, String... elm) throws Exception {
		IBMFhirServerConfig fhirConfig = new IBMFhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");
		fhirConfig.setUser("fhiruser");
		fhirConfig.setPassword("change-password");
		fhirConfig.setTenantId("default");

		return setupTestFor(patient, fhirConfig, elm);
	}

	private CqlEngineWrapper setupTestFor(Patient patient, FhirServerConfig fhirConfig, String... elm)
			throws Exception {
		CapabilityStatement metadata = getCapabilityStatement();

		FhirContext fhirContext = FhirContext.forR4();
		IParser encoder = fhirContext.newJsonParser().setPrettyPrint(true);

		mockFhirResourceRetrieval("/metadata", encoder, metadata, fhirConfig);

		mockFhirResourceRetrieval("/Patient/" + patient.getId(), encoder, patient, fhirConfig);

		CqlEngineWrapper wrapper = new CqlEngineWrapper();
		if (elm != null) {
			for (String resource : elm) {
				try (InputStream is = ClassLoader.getSystemResourceAsStream(resource)) {
					wrapper.addLibrary(is, LibraryFormat.forString(resource), null);
				}
			}
		}

		wrapper.setDataServerConnectionProperties(fhirConfig);
		wrapper.setTerminologyServerConnectionProperties(fhirConfig);
		return wrapper;
	}

	protected CapabilityStatement getCapabilityStatement() {
		CapabilityStatement metadata = new CapabilityStatement();
		metadata.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
		return metadata;
	}

	private void mockFhirResourceRetrieval(String resourcePath, IParser encoder, Resource resource,
			FhirServerConfig fhirConfig) {
		MappingBuilder builder = get(urlEqualTo(resourcePath));
		if (fhirConfig.getUser() != null && fhirConfig.getPassword() != null) {
			builder = builder.withBasicAuth(fhirConfig.getUser(), fhirConfig.getPassword());
		}

		Map<String, String> additionalHeaders = fhirConfig.getAdditionalHeaders();
		if (additionalHeaders != null) {
			for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
				builder = builder.withHeader(header.getKey(), matching(header.getValue()));
			}
		}

		stubFor(builder.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
				.withBody(encoder.encodeResourceToString(resource))));
	}

	@Test
	public void testResolveIntegerParameter() {
		Map<String, Object> params = CqlEngineWrapper.parseParameters(Arrays.asList("test:integer:40"));
		assertEquals(1, params.size());
		Integer p = (Integer) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 40, p.intValue());
	}

	@Test
	public void testResolveDecimalParameter() {
		Map<String, Object> params = CqlEngineWrapper.parseParameters(Arrays.asList("test:decimal:40.0"));
		assertEquals(1, params.size());
		BigDecimal p = (BigDecimal) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 40, p.intValue());
	}

	@Test
	public void testResolveBooleanParameter() {
		Map<String, Object> params = CqlEngineWrapper.parseParameters(Arrays.asList("test:boolean:true"));
		assertEquals(1, params.size());
		Boolean p = (Boolean) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", true, p.booleanValue());
	}

	@Test
	public void testResolveStringParameter() {
		Map<String, Object> params = CqlEngineWrapper
				.parseParameters(Arrays.asList("test:string:I have the:delimiter"));
		assertEquals(1, params.size());
		String p = (String) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "I have the:delimiter", p);
	}

	@Test
	public void testResolveDateTimeParameter() {
		Map<String, Object> params = CqlEngineWrapper
				.parseParameters(Arrays.asList("test:datetime:@2020-09-27T12:13:14"));
		assertEquals(1, params.size());
		DateTime p = (DateTime) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 2020, p.getDateTime().getYear());
		assertEquals("Unexpected value", 9, p.getDateTime().getMonthValue());
		assertEquals("Unexpected value", 27, p.getDateTime().getDayOfMonth());
		assertEquals("Unexpected value", 12, p.getDateTime().getHour());
		assertEquals("Unexpected value", 13, p.getDateTime().getMinute());
		assertEquals("Unexpected value", 14, p.getDateTime().getSecond());
	}

	@Test
	public void testResolveTimeParameter() {
		Map<String, Object> params = CqlEngineWrapper.parseParameters(Arrays.asList("test:time:T12:13:14"));
		assertEquals(1, params.size());
		Time p = (Time) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", 12, p.getTime().getHour());
		assertEquals("Unexpected value", 13, p.getTime().getMinute());
		assertEquals("Unexpected value", 14, p.getTime().getSecond());
	}

	@Test
	public void testResolveQuantityParameter() {
		Map<String, Object> params = CqlEngineWrapper.parseParameters(Arrays.asList("test:quantity:100:mg/mL"));
		assertEquals(1, params.size());
		Quantity p = (Quantity) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "100", p.getValue().toString());
		assertEquals("Unexpected value", "mg/mL", p.getUnit().toString());
	}

	@Test
	public void testResolveCodeParameter() {
		Map<String, Object> params = CqlEngineWrapper.parseParameters(Arrays.asList("test:code:1.2.3:SNOMEDCT:Hernia"));
		assertEquals(1, params.size());
		Code p = (Code) params.get("test");
		assertNotNull("Parameter with expected name not found", p);
		assertEquals("Unexpected value", "1.2.3", p.getCode());
		assertEquals("Unexpected value", "SNOMEDCT", p.getSystem());
		assertEquals("Unexpected value", "Hernia", p.getDisplay());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testResolveConceptParameter() {
		CqlEngineWrapper.parseParameters(Arrays.asList("test:concept:not right now"));
	}

	@Test
	public void testResolveIntervalIntegerParameter() {
		Map<String, Object> params = CqlEngineWrapper.parseParameters(Arrays.asList("test:interval:integer,10,20"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		assertEquals(10, p.getStart());
		assertEquals(20, p.getEnd());
	}

	@Test
	public void testResolveIntervalDecimalParameter() {
		Map<String, Object> params = CqlEngineWrapper.parseParameters(Arrays.asList("test:interval:decimal,10,20"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		assertEquals(10, ((BigDecimal) p.getStart()).intValue());
		assertEquals(20, ((BigDecimal) p.getEnd()).intValue());
	}

	@Test
	public void testResolveIntervalQuantityParameter() {
		Map<String, Object> params = CqlEngineWrapper
				.parseParameters(Arrays.asList("test:interval:quantity,10:mg/mL,20:mg/mL"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		assertEquals(10, ((Quantity) p.getStart()).getValue().intValue());
		assertEquals("mg/mL", ((Quantity) p.getStart()).getUnit());
		assertEquals(20, ((Quantity) p.getEnd()).getValue().intValue());
		assertEquals("mg/mL", ((Quantity) p.getEnd()).getUnit());
	}

	@Test
	public void testResolveIntervalDatetimeParameter() {
		Map<String, Object> params = CqlEngineWrapper
				.parseParameters(Arrays.asList("test:interval:datetime,@2020-01-02T12:13:14,@2021-02-03T22:33:44"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		DateTime start = (DateTime) p.getStart();
		assertEquals(2020, start.getDateTime().getYear());
		assertEquals(1, start.getDateTime().getMonthValue());
		assertEquals(2, start.getDateTime().getDayOfMonth());
		assertEquals(12, start.getDateTime().getHour());
		assertEquals(13, start.getDateTime().getMinute());
		assertEquals(14, start.getDateTime().getSecond());

		DateTime end = (DateTime) p.getEnd();
		assertEquals(2021, end.getDateTime().getYear());
		assertEquals(2, end.getDateTime().getMonthValue());
		assertEquals(3, end.getDateTime().getDayOfMonth());
		assertEquals(22, end.getDateTime().getHour());
		assertEquals(33, end.getDateTime().getMinute());
		assertEquals(44, end.getDateTime().getSecond());
	}

	@Test
	public void testResolveIntervalTimeParameter() {
		Map<String, Object> params = CqlEngineWrapper
				.parseParameters(Arrays.asList("test:interval:time,T12:13:14,T22:33:44"));
		assertEquals(1, params.size());
		Interval p = (Interval) params.get("test");
		Time start = (Time) p.getStart();
		assertEquals(12, start.getTime().getHour());
		assertEquals(13, start.getTime().getMinute());
		assertEquals(14, start.getTime().getSecond());

		Time end = (Time) p.getEnd();
		assertEquals(22, end.getTime().getHour());
		assertEquals(33, end.getTime().getMinute());
		assertEquals(44, end.getTime().getSecond());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolveIntervalUnsupportedSubtypeParameter() {
		CqlEngineWrapper.parseParameters(Arrays.asList("test:interval:unsupported,a,b"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolveUnsupportedTypeParameter() {
		CqlEngineWrapper.parseParameters(Arrays.asList("test:unsupported:a,b"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolveUnsupportedFormatParameter() {
		CqlEngineWrapper.parseParameters(Arrays.asList("gibberish"));
	}

	@Test
	public void testSimpleFhirConfig() {
		FhirServerConfig simple = new FhirServerConfig();
		simple.setEndpoint("http://hapi.fhir.org/baseR4");

		FhirContext ctx = FhirContext.forR4();
		CqlEngineWrapper wrapper = new CqlEngineWrapper();
		IGenericClient client = wrapper.initializeFhirClient(ctx, simple);
		assertEquals(0, client.getInterceptorService().getAllRegisteredInterceptors().size());
	}
}
