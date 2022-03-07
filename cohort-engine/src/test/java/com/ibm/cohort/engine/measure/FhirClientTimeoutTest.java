/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;


import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;

public class FhirClientTimeoutTest extends BaseFhirTest {

	private static final String PATIENT_ID = "TimeoutTest-PatientId";
	private static final int PATIENT_RETRIEVAL_DELAY_MILLIS = 200;
	private static final int CONFIG_TIMEOUT_MILLIS = 100;
	private static final int CONFIG_NO_TIMEOUT_MILLIS = 20_000;
	
	@Before
	public void setup() {
		mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());
		mockPatientRetrievalTimeout(PATIENT_ID, Enumerations.AdministrativeGender.OTHER, 22, PATIENT_RETRIEVAL_DELAY_MILLIS);
	}
	
	@Test(expected = FhirClientConnectionException.class)
	public void testFhirClientContext_requestTimesOut() {
		FhirServerConfig fhirServerConfig = getFhirServerConfig();
		fhirServerConfig.setSocketTimeout(CONFIG_TIMEOUT_MILLIS);

		FHIRClientContext fhirClientContext = new FHIRClientContext.Builder()
				.withDefaultClient(fhirServerConfig)
				.build();
		
		fhirClientContext.getDataClient().read()
				.resource(Patient.class)
				.withId(PATIENT_ID)
				.execute();
	}

	@Test
	public void testFhirClientContext_handlesDelayWithConfiguration() {
		FhirServerConfig fhirServerConfig = getFhirServerConfig();
		fhirServerConfig.setSocketTimeout(CONFIG_NO_TIMEOUT_MILLIS);

		FHIRClientContext fhirClientContext = new FHIRClientContext.Builder()
				.withDefaultClient(fhirServerConfig)
				.build();

		Patient patient = fhirClientContext.getDataClient().read()
				.resource(Patient.class)
				.withId(PATIENT_ID)
				.execute();
		
		Enumerations.AdministrativeGender actual = patient.getGender();
		Assert.assertEquals(Enumerations.AdministrativeGender.OTHER, actual);
	}

	@Test
	public void testFhirClientContext_handlesDelayDefault() {
		FhirServerConfig fhirServerConfig = getFhirServerConfig();
		fhirServerConfig.setSocketTimeout(null);

		FHIRClientContext fhirClientContext = new FHIRClientContext.Builder()
				.withDefaultClient(fhirServerConfig)
				.build();

		Patient patient = fhirClientContext.getDataClient().read()
				.resource(Patient.class)
				.withId(PATIENT_ID)
				.execute();

		Enumerations.AdministrativeGender actual = patient.getGender();
		Assert.assertEquals(Enumerations.AdministrativeGender.OTHER, actual);
	}

	@Test(expected = FhirClientConnectionException.class)
	public void testDefaultFhirClientBuilder_requestTimesOut() {
		FhirServerConfig fhirServerConfig = getFhirServerConfig();
		fhirServerConfig.setSocketTimeout(CONFIG_TIMEOUT_MILLIS);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(fhirContext);
		IGenericClient client = builder.createFhirClient(fhirServerConfig);

		client.read()
				.resource(Patient.class)
				.withId(PATIENT_ID)
				.execute();
	}

	@Test
	public void testDefaultFhirClientBuilder_handlesDelayWithConfiguration() {
		FhirServerConfig fhirServerConfig = getFhirServerConfig();
		fhirServerConfig.setSocketTimeout(CONFIG_NO_TIMEOUT_MILLIS);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(fhirContext);
		IGenericClient client = builder.createFhirClient(fhirServerConfig);

		Patient patient = client.read()
				.resource(Patient.class)
				.withId(PATIENT_ID)
				.execute();

		Enumerations.AdministrativeGender actual = patient.getGender();
		Assert.assertEquals(Enumerations.AdministrativeGender.OTHER, actual);
	}

	@Test
	public void testDefaultFhirClientBuilder_handlesDelayDefault() {
		FhirServerConfig fhirServerConfig = getFhirServerConfig();
		fhirServerConfig.setSocketTimeout(null);

		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(fhirContext);
		IGenericClient client = builder.createFhirClient(fhirServerConfig);

		Patient patient = client.read()
				.resource(Patient.class)
				.withId(PATIENT_ID)
				.execute();

		Enumerations.AdministrativeGender actual = patient.getGender();
		Assert.assertEquals(Enumerations.AdministrativeGender.OTHER, actual);
	}
}
