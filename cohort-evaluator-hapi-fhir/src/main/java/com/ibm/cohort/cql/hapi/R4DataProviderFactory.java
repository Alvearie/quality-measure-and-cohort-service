/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.data.DefaultCqlDataProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.fhir.exception.FhirVersionMisMatchException;
import org.opencds.cqf.cql.engine.fhir.retrieve.BaseFhirQueryGenerator;
import org.opencds.cqf.cql.engine.fhir.retrieve.FhirQueryGeneratorFactory;
import org.opencds.cqf.cql.engine.fhir.retrieve.RestFhirRetrieveProvider;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;

import com.ibm.cohort.cql.cache.CachingRetrieveProvider;
import com.ibm.cohort.cql.cache.RetrieveCacheContext;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * <p>An internal class intended to ease the burden of {@link DataProvider}
 * creation and URI mapping necessary for cohort and measure evaluation.
 */
public class R4DataProviderFactory {

	/**
	 * This is used to control whether the data provider tries to use the 
	 * token:in modifier to do ValueSet expansion on the FHIR server or
	 * uses the TerminologyProvider to expand the ValueSet prior to 
	 * calling the FHIR search API to retrieve data.
	 */
	public static final boolean DEFAULT_IS_EXPAND_VALUE_SETS = true;
	
	/**
	 * This controls how many records to request in a FHIR search query. By default
	 * the FHIR servers tend to use very small numbers (HAPI:20, IBM:10) which
	 * causes a large number of network requests for even moderately sized datasets.
	 * This default matches the largest allowed page size in the IBM FHIR server.
	 */
	public static final Integer DEFAULT_PAGE_SIZE = 1000;
	
	public static final String FHIR_R4_URL = "http://hl7.org/fhir";

	public static Map<String, CqlDataProvider> createDataProviderMap(
			IGenericClient client,
			CqlTerminologyProvider terminologyProvider,
			RetrieveCacheContext retrieveCacheContext
	) {
		return createDataProviderMap(
				client,
				terminologyProvider,
				retrieveCacheContext,
				R4FhirModelResolverFactory.createCachingResolver(),
				DEFAULT_IS_EXPAND_VALUE_SETS,
				DEFAULT_PAGE_SIZE
		);
	}

	public static Map<String, CqlDataProvider> createDataProviderMap(
			IGenericClient client,
			CqlTerminologyProvider terminologyProvider,
			RetrieveCacheContext retrieveCacheContext,
			ModelResolver modelResolver,
			boolean isExpandValueSets,
			Integer pageSize
	) {
		CqlDataProvider dataProvider = createDataProvider(
				client,
				terminologyProvider,
				retrieveCacheContext,
				modelResolver,
				isExpandValueSets,
				pageSize
		);

		Map<String, CqlDataProvider> retVal = new HashMap<>();
		retVal.put(FHIR_R4_URL, dataProvider);

		return retVal;
	}

	public static CqlDataProvider createDataProvider(
			IGenericClient client,
			CqlTerminologyProvider terminologyProvider,
			RetrieveCacheContext retrieveCacheContext
	) {
		return createDataProvider(
				client,
				terminologyProvider,
				retrieveCacheContext,
				R4FhirModelResolverFactory.createCachingResolver(),
				DEFAULT_IS_EXPAND_VALUE_SETS,
				DEFAULT_PAGE_SIZE
		);
	}

	public static CqlDataProvider createDataProvider(
			IGenericClient client,
			CqlTerminologyProvider terminologyProvider,
			RetrieveCacheContext retrieveCacheContext,
			ModelResolver modelResolver,
			boolean isExpandValueSets,
			Integer pageSize
	) {
		SearchParameterResolver resolver = new SearchParameterResolver(client.getFhirContext());
		RestFhirRetrieveProvider baseRetrieveProvider = new RestFhirRetrieveProvider(resolver, client);
		try {
			BaseFhirQueryGenerator queryGenerator = FhirQueryGeneratorFactory.create(modelResolver, resolver, terminologyProvider);
			baseRetrieveProvider.setFhirQueryGenerator(queryGenerator);
		} catch (FhirVersionMisMatchException e) {
			throw new IllegalArgumentException("Unsupported FHIR version", e);
		}
		baseRetrieveProvider.setExpandValueSets(isExpandValueSets);
		if(pageSize != null && pageSize > 0) {
			baseRetrieveProvider.setPageSize(pageSize);
		}
		baseRetrieveProvider.setTerminologyProvider(terminologyProvider);
		RetrieveProvider retrieveProvider = retrieveCacheContext != null
				? new CachingRetrieveProvider(baseRetrieveProvider, retrieveCacheContext)
				: baseRetrieveProvider;

		return new DefaultCqlDataProvider(modelResolver, retrieveProvider);
	}

}
