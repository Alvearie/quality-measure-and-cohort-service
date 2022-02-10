/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import java.util.Map;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.hapi.R4LibraryDependencyGatherer;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.resolver.R4FhirServerResrouceResolverFactory;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.cql.engine.model.ModelResolver;

import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.r4.cache.CachingModelResolverDecorator;
import com.ibm.cohort.engine.r4.cache.R4FhirModelResolverFactory;
import com.ibm.cohort.engine.terminology.R4RestFhirTerminologyProvider;

/**
 * A builder intended to allow for easy creation of {@link MeasureEvaluator} instances tailored for use
 * with FHIR R4.
 */
public class R4MeasureEvaluatorBuilder {

	/* NOTE:
	 * This class was moved to the `test` folder as it was only used by tests at the time.
	 * Move this back to the `source` folder if you have need of it in our actual code.
	 */

	private FHIRClientContext clientContext;
	private RetrieveCacheContext cacheContext;
	private boolean isExpandValueSets = R4DataProviderFactory.DEFAULT_IS_EXPAND_VALUE_SETS;
	private Integer pageSize = R4DataProviderFactory.DEFAULT_PAGE_SIZE;
	private Boolean isCachingModelResolver = false;
	private ModelResolver modelResolver = R4FhirModelResolverFactory.createNonCachingResolver();

	public R4MeasureEvaluatorBuilder withClientContext(FHIRClientContext value) {
		this.clientContext = value;
		return this;
	}

	public R4MeasureEvaluatorBuilder withRetrieveCacheContext(RetrieveCacheContext value) {
		this.cacheContext = value;
		return this;
	}
	
	public R4MeasureEvaluatorBuilder withExpandValueSets(boolean value) {
		this.isExpandValueSets = value;
		return this;
	}
	
	public R4MeasureEvaluatorBuilder withPageSize(Integer value) {
		this.pageSize = value;
		return this;
	}

	public R4MeasureEvaluatorBuilder withModelResolverCaching(Boolean isCachingModelResolver) {
		this.isCachingModelResolver = isCachingModelResolver;
		return this;
	}

	public MeasureEvaluator build() {
		if (clientContext == null) {
			throw new IllegalArgumentException("Client context not provided");
		}

		CqlTerminologyProvider terminologyProvider = new R4RestFhirTerminologyProvider(clientContext.getTerminologyClient());
		Map<String, CqlDataProvider> dataProviders = R4DataProviderFactory.createDataProviderMap(
				clientContext.getDataClient(),
				terminologyProvider,
				cacheContext,
				isCachingModelResolver ? new CachingModelResolverDecorator(modelResolver) : modelResolver,
				isExpandValueSets,
				pageSize
		);

		FhirResourceResolver<Measure> measureResolver = R4FhirServerResrouceResolverFactory.createMeasureResolver(clientContext.getMeasureClient());
		FhirResourceResolver<Library> libraryResolver = R4FhirServerResrouceResolverFactory.createLibraryResolver(clientContext.getLibraryClient());
		R4LibraryDependencyGatherer libraryDependencyGatherer = new R4LibraryDependencyGatherer(libraryResolver);

		return new MeasureEvaluator(measureResolver, libraryResolver, libraryDependencyGatherer, terminologyProvider, dataProviders);
	}

}
