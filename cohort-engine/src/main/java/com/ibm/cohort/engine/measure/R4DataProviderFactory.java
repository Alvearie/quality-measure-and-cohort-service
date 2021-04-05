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

	/**
	 * As of the current 4.6.0 version, IBM FHIR does not support the 
	 * token:in modifier, so the default behavior is to to expand value
	 * sets outside of FHIR for code-based queries. I discussed with the 
	 * FHIR team how we might determine this capability dynamically via
	 * the CapabilityStatement, but doing so is not well supported in 
	 * either IBM FHIR or HAPI FHIR right now, so we are going with
	 * this hard-coding approach for now. API consumers can use the
	 * parameterized method as needed. 
	 */
	public static final boolean DEFAULT_IS_EXPAND_VALUE_SETS = true;
	
	protected static final String FHIR_R4_URL = "http://hl7.org/fhir";
	
	public static Map<String, DataProvider> createDataProviderMap(
			IGenericClient client,
			TerminologyProvider terminologyProvider,
			RetrieveCacheContext retrieveCacheContext,
			boolean isExpandValueSets
	) {
		ModelResolver modelResolver = new R4FhirModelResolver();

		SearchParameterResolver resolver = new SearchParameterResolver(client.getFhirContext());
		RestFhirRetrieveProvider baseRetrieveProvider = new RestFhirRetrieveProvider(resolver, client);
		baseRetrieveProvider.setExpandValueSets(isExpandValueSets);
		baseRetrieveProvider.setTerminologyProvider(terminologyProvider);
		RetrieveProvider retrieveProvider = retrieveCacheContext != null
				? new CachingRetrieveProvider(baseRetrieveProvider, retrieveCacheContext)
				: baseRetrieveProvider;

		DataProvider dataProvider = new CompositeDataProvider(modelResolver, retrieveProvider);

		Map<String, DataProvider> retVal = new HashMap<>();
		retVal.put(FHIR_R4_URL, dataProvider);

		return retVal;
	}
	
	public static Map<String, DataProvider> createDataProviderMap(
			IGenericClient client,
			TerminologyProvider terminologyProvider,
			RetrieveCacheContext retrieveCacheContext
	) {
		return createDataProviderMap( client, terminologyProvider, retrieveCacheContext, DEFAULT_IS_EXPAND_VALUE_SETS );
	}

}
