/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.hapi.FhirTestBase;
import com.ibm.cohort.cql.hapi.R4DataProviderFactory;
import com.ibm.cohort.cql.hapi.R4RestFhirTerminologyProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.cql.engine.data.DataProvider;

import com.ibm.cohort.cql.cache.DefaultRetrieveCacheContext;
import com.ibm.cohort.cql.cache.RetrieveCacheContext;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class R4DataProviderFactoryTest extends FhirTestBase {

	private static final String PATIENT_ID = "R4DataProviderFactoryTest-PatientId";

	@Before
	public void setup() {
		mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());
		mockPatientRetrieval(PATIENT_ID, AdministrativeGender.OTHER, 22);
	}

	@Test
	public void createDataProviderMap_noCacheContext() {
		FhirClientBuilderFactory factory = new DefaultFhirClientBuilderFactory();
		IGenericClient client = factory.newFhirClientBuilder().createFhirClient(getFhirServerConfig());
		CqlTerminologyProvider terminologyProvider = new R4RestFhirTerminologyProvider(client);
		Map<String, CqlDataProvider> map = R4DataProviderFactory.createDataProviderMap(
				client,
				terminologyProvider,
				null
		);
		verifyDataProviderMap(map);
	}

	@Test
	public void createDataProviderMap_withCacheContext() throws Exception {
		FhirClientBuilderFactory factory = new DefaultFhirClientBuilderFactory();
		IGenericClient client = factory.newFhirClientBuilder().createFhirClient(getFhirServerConfig());
		CqlTerminologyProvider terminologyProvider = new R4RestFhirTerminologyProvider(client);
		try(RetrieveCacheContext cacheContext = new DefaultRetrieveCacheContext(new CaffeineConfiguration<>())) {
			Map<String, CqlDataProvider> map = R4DataProviderFactory.createDataProviderMap(
					client,
					terminologyProvider,
					cacheContext
			);
			verifyDataProviderMap(map);
		}
	}

	private void verifyDataProviderMap(Map<String, CqlDataProvider> dataProviderMap) {
		Assert.assertEquals(1, dataProviderMap.size());
		DataProvider dataProvider = dataProviderMap.get(R4DataProviderFactory.FHIR_R4_URL);
		Iterable<Object> iterable = dataProvider.retrieve(
				"Patient",
				"id",
				PATIENT_ID,
				"Patient",
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);
		List<Object> list = new ArrayList<>();
		iterable.forEach(list::add);
		Assert.assertEquals(1, list.size());

		Object possiblePatient = list.get(0);
		Assert.assertTrue("Returned value not type Patient", possiblePatient instanceof Patient);

		Patient patient = (Patient)possiblePatient;
		AdministrativeGender actual = patient.getGender();
		Assert.assertEquals(AdministrativeGender.OTHER, actual);
	}

}
