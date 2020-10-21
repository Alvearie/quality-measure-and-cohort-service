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

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class CqlTestUtil {

	protected static void mockFhirResourceRetrieval(String resourcePath, IParser encoder, Resource resource,
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

	protected static Patient getPatient(String id, Enumerations.AdministrativeGender administrativeGender, String birthDateStr)
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

	protected static IParser getFhirParser() {
		FhirContext fhirContext = FhirContext.forR4();
		IParser encoder = fhirContext.newJsonParser().setPrettyPrint(true);
		return encoder;
	}

	protected static CqlEngineWrapper setupTestFor(Patient patient, String... elm) throws Exception {
		IBMFhirServerConfig fhirConfig = new IBMFhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:8089");
		fhirConfig.setUser("fhiruser");
		fhirConfig.setPassword("change-password");
		fhirConfig.setTenantId("default");

		return setupTestFor(patient, fhirConfig, elm);
	}

	protected static CqlEngineWrapper setupTestFor(Patient patient, FhirServerConfig fhirConfig, String... elm) throws Exception {
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
		wrapper.setMeasureServerConnectionProperties(fhirConfig);
		return wrapper;
	}

	protected static CapabilityStatement getCapabilityStatement() {
		CapabilityStatement metadata = new CapabilityStatement();
		metadata.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
		return metadata;
	}
}
