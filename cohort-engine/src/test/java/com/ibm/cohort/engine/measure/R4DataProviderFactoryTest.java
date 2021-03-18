/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.measure.cache.DefaultRetrieveCacheContext;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.fhir.terminology.R4FhirTerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class R4DataProviderFactoryTest extends BaseFhirTest {

	private static final String PATIENT_ID = "R4DataProviderFactoryTest-PatientId";

	@Before
	public void setup() {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		mockPatientRetrieval(PATIENT_ID, AdministrativeGender.OTHER, 22);
	}

	@Test
	public void createDataProviderMap_noCacheContext() {
		IGenericClient client = new FHIRClientContext.Builder()
				.withDefaultClient(getFhirServerConfig())
				.build()
				.getDataClient();
		TerminologyProvider terminologyProvider = new R4FhirTerminologyProvider(client);
		Map<String, DataProvider> map = R4DataProviderFactory.createDataProviderMap(
				client,
				terminologyProvider,
				null
		);
		verifyDataProviderMap(map);
	}

	@Test
	public void createDataProviderMap_withCacheContext() throws Exception {
		IGenericClient client = new FHIRClientContext.Builder()
				.withDefaultClient(getFhirServerConfig())
				.build()
				.getDataClient();
		TerminologyProvider terminologyProvider = new R4FhirTerminologyProvider(client);
		try(RetrieveCacheContext cacheContext = new DefaultRetrieveCacheContext(new CaffeineConfiguration<>())) {
			Map<String, DataProvider> map = R4DataProviderFactory.createDataProviderMap(
					client,
					terminologyProvider,
					cacheContext
			);
			verifyDataProviderMap(map);
		}
	}

	private void verifyDataProviderMap(Map<String, DataProvider> dataProviderMap) {
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
