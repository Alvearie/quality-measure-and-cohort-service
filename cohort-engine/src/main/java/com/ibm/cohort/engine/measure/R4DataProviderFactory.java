/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.ibm.cohort.engine.measure.cache.CachingRetrieveProvider;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.retrieve.RestFhirRetrieveProvider;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import java.util.HashMap;
import java.util.Map;

public class R4DataProviderFactory {

	private static final String FHIR_R4_URL = "http://hl7.org/fhir";

	public static Map<String, DataProvider> createDataProviderMap(
			IGenericClient client,
			TerminologyProvider terminologyProvider,
			RetrieveCacheContext cacheContext
	) {
		// TODO: We no longer need the threadlocal...right??
		ModelResolver modelResolver = new R4FhirModelResolver();

		SearchParameterResolver resolver = new SearchParameterResolver(client.getFhirContext());
		RestFhirRetrieveProvider baseRetrieveProvider = new RestFhirRetrieveProvider(resolver, client);
		baseRetrieveProvider.setTerminologyProvider(terminologyProvider);
		RetrieveProvider retrieveProvider = cacheContext != null
				? new CachingRetrieveProvider(baseRetrieveProvider, cacheContext)
				: baseRetrieveProvider;

		DataProvider dataProvider = new CompositeDataProvider(modelResolver, retrieveProvider);

		Map<String, DataProvider> retVal = new HashMap<>();
		retVal.put(FHIR_R4_URL, dataProvider);

		return retVal;
	}

}
