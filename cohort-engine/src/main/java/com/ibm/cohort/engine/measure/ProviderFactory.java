/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.measure.cache.CachingRetrieveProvider;
import org.opencds.cqf.common.evaluation.EvaluationProviderFactory;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.retrieve.RestFhirRetrieveProvider;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.fhir.terminology.R4FhirTerminologyProvider;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Implement the EvaluationProviderFactory for our use case. The only
 * supported implementation right now is for FHIR R4-based models,
 * via REST API interaction and FHIR terminology support. This will
 * evolve as needed to support our performance considerations.
 */
public class ProviderFactory implements EvaluationProviderFactory {

	/*
	 * Wrap the ModelResolver around a static ThreadLocal to prevent
	 * excess creation of FhirContext instances.
	 */
	private static final ThreadLocal<ModelResolver> MODEL_RESOLVER = ThreadLocal.withInitial(R4FhirModelResolver::new);

	private IGenericClient dataClient;
	private IGenericClient terminologyClient;
	private RetrieveCacheContext retrieveCacheContext;

	public ProviderFactory(IGenericClient dataClient, IGenericClient terminologyClient, RetrieveCacheContext retrieveCacheContext) {
		this.dataClient = dataClient;
		this.terminologyClient = terminologyClient;
		this.retrieveCacheContext = retrieveCacheContext;
	}
	
	@Override
	public DataProvider createDataProvider(String model, String version) {
		//TODO: throw an error for an unsupported model and/or version
		ModelResolver modelResolver = MODEL_RESOLVER.get();
		SearchParameterResolver resolver = new SearchParameterResolver(this.dataClient.getFhirContext());

		RetrieveProvider baseRetrieveProvider = new RestFhirRetrieveProvider(resolver, this.dataClient);
		RetrieveProvider retrieveProvider = retrieveCacheContext != null
				? new CachingRetrieveProvider(baseRetrieveProvider, retrieveCacheContext)
				: baseRetrieveProvider;

		CompositeDataProvider dataProvider = new CompositeDataProvider(modelResolver, retrieveProvider);
		return dataProvider;
	}

	@Override
	public DataProvider createDataProvider(String model, String version, String url, String user, String pass) {
		return createDataProvider( model, version );
	}

	@Override
	public DataProvider createDataProvider(String model, String version, TerminologyProvider terminologyProvider) {
		//TODO: throw an error for an unsupported model and/or version
		ModelResolver modelResolver = MODEL_RESOLVER.get();
		SearchParameterResolver resolver = new SearchParameterResolver(this.dataClient.getFhirContext());
		
		//TODO: plug in our own retrieve provider when it becomes a thing
		RestFhirRetrieveProvider baseRetrieveProvider = new RestFhirRetrieveProvider(resolver, this.dataClient);
		baseRetrieveProvider.setTerminologyProvider(terminologyProvider);
		RetrieveProvider retrieveProvider = retrieveCacheContext != null
				? new CachingRetrieveProvider(baseRetrieveProvider, retrieveCacheContext)
				: baseRetrieveProvider;

		CompositeDataProvider dataProvider = new CompositeDataProvider(modelResolver, retrieveProvider);
		return dataProvider;
	}

	@Override
	public TerminologyProvider createTerminologyProvider(String model, String version, String url, String user,
			String pass) {
		TerminologyProvider termProvider = new R4FhirTerminologyProvider(this.terminologyClient);
		return termProvider;
	}
}
