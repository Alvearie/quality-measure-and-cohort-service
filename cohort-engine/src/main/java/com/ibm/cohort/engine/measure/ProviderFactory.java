/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

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

	public ProviderFactory(IGenericClient dataClient, IGenericClient terminologyClient) {
		this.dataClient = dataClient;
		this.terminologyClient = terminologyClient;
	}
	
	@Override
	public DataProvider createDataProvider(String model, String version) {
		//TODO: throw an error for an unsupported model and/or version
		ModelResolver modelResolver = new R4FhirModelResolver();
		SearchParameterResolver resolver = new SearchParameterResolver(this.dataClient.getFhirContext());
		//TODO: plug in our own retrieve provider when it becomes a thing
		RetrieveProvider retrieveProvider = new RestFhirRetrieveProvider(resolver, this.dataClient);
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
		RestFhirRetrieveProvider retrieveProvider = new RestFhirRetrieveProvider(resolver, this.dataClient);
		retrieveProvider.setTerminologyProvider(terminologyProvider);
		
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
