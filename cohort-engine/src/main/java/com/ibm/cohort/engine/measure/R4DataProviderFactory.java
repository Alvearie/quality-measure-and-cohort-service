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

/**
 * <p>An internal class intended to ease the burden of creating the URI to {@link DataProvider} mapping
 * required by the {@link MeasureEvaluator}.
 *
 * <p>This should only be used if the {@link R4MeasureEvaluatorBuilder} is insufficient for your usecase.
 */
public class R4DataProviderFactory {

	protected static final String FHIR_R4_URL = "http://hl7.org/fhir";

	public static Map<String, DataProvider> createDataProviderMap(
			IGenericClient client,
			TerminologyProvider terminologyProvider,
			RetrieveCacheContext retrieveCacheContext
	) {
		ModelResolver modelResolver = new R4FhirModelResolver();

		SearchParameterResolver resolver = new SearchParameterResolver(client.getFhirContext());
		RestFhirRetrieveProvider baseRetrieveProvider = new RestFhirRetrieveProvider(resolver, client);
		baseRetrieveProvider.setTerminologyProvider(terminologyProvider);
		RetrieveProvider retrieveProvider = retrieveCacheContext != null
				? new CachingRetrieveProvider(baseRetrieveProvider, retrieveCacheContext)
				: baseRetrieveProvider;

		DataProvider dataProvider = new CompositeDataProvider(modelResolver, retrieveProvider);

		Map<String, DataProvider> retVal = new HashMap<>();
		retVal.put(FHIR_R4_URL, dataProvider);

		return retVal;
	}

}
