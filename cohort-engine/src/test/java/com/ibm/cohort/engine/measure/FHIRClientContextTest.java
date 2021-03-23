/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.ibm.cohort.engine.BaseFhirTest;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FHIRClientContextTest extends BaseFhirTest {

	private static final String PATIENT_ID = "FHIRClientContextTest-PatientId";

	@Before
	public void setup() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		mockPatientRetrieval(PATIENT_ID, AdministrativeGender.OTHER, 22);
	}

	@Test
	public void getClients_onlyDefaultClientConfigs() {
		FHIRClientContext context = new FHIRClientContext.Builder()
				.withDefaultClient(getFhirServerConfig())
				.build();

		verifyClients(context);
	}

	@Test
	public void getClients_onlySpecificClientConfigs() {
		FHIRClientContext context = new FHIRClientContext.Builder()
				.withDataClient(getFhirServerConfig())
				.withTerminologyClient(getFhirServerConfig())
				.withMeasureClient(getFhirServerConfig())
				.withLibraryClient(getFhirServerConfig())
				.build();

		verifyClients(context);
	}

	@Test
	public void getClients_defaultAndSpecificClientConfigs() {
		FHIRClientContext context = new FHIRClientContext.Builder()
				.withDefaultClient(getFhirServerConfig())
				.withDataClient(getFhirServerConfig())
				.withTerminologyClient(getFhirServerConfig())
				.withMeasureClient(getFhirServerConfig())
				.withLibraryClient(getFhirServerConfig())
				.build();

		verifyClients(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void build_missingDataClient() {
		new FHIRClientContext.Builder()
				.withTerminologyClient(getFhirServerConfig())
				.withMeasureClient(getFhirServerConfig())
				.withLibraryClient(getFhirServerConfig())
				.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void build_missingTerminologyClient() {
		new FHIRClientContext.Builder()
				.withDataClient(getFhirServerConfig())
				.withMeasureClient(getFhirServerConfig())
				.withLibraryClient(getFhirServerConfig())
				.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void build_missingMeasureClient() {
		new FHIRClientContext.Builder()
				.withDataClient(getFhirServerConfig())
				.withTerminologyClient(getFhirServerConfig())
				.withLibraryClient(getFhirServerConfig())
				.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void build_missingLibraryClient() {
		new FHIRClientContext.Builder()
				.withDataClient(getFhirServerConfig())
				.withTerminologyClient(getFhirServerConfig())
				.withMeasureClient(getFhirServerConfig())
				.build();
	}

	private void verifyClients(FHIRClientContext context) {
		verifyClient(context.getDataClient());
		verifyClient(context.getTerminologyClient());
		verifyClient(context.getMeasureClient());
		verifyClient(context.getLibraryClient());
	}

	private void verifyClient(IGenericClient client) {
		Patient patient = client
				.read()
				.resource(Patient.class)
				.withId(PATIENT_ID)
				.execute();
		AdministrativeGender actual = patient.getGender();
		Assert.assertEquals(AdministrativeGender.OTHER, actual);
	}

}
