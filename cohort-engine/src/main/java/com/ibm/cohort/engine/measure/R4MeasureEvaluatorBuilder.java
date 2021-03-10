/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.fhir.terminology.R4FhirTerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import java.util.Map;

public class R4MeasureEvaluatorBuilder {

	private FHIRClientContext clientContext;
	private RetrieveCacheContext cacheContext;

	public R4MeasureEvaluatorBuilder withClientContext(FHIRClientContext value) {
		this.clientContext = value;
		return this;
	}

	public R4MeasureEvaluatorBuilder withRetrieveCacheContext(RetrieveCacheContext value) {
		this.cacheContext = value;
		return this;
	}

	public MeasureEvaluator build() {
		if (clientContext == null) {
			// TODO: Throw real error
			throw new RuntimeException("Client context not set");
		}

		TerminologyProvider terminologyProvider = new R4FhirTerminologyProvider(clientContext.getTerminologyClient());
		Map<String, DataProvider> dataProviders = R4DataProviderFactory.createDataProviderMap(
				clientContext.getDataClient(),
				terminologyProvider,
				cacheContext
		);
		MeasureResolutionProvider<Measure> measureProvider = new RestFhirMeasureResolutionProvider(clientContext.getMeasureClient());
		LibraryResolutionProvider<Library> libraryProvider = new RestFhirLibraryResolutionProvider(clientContext.getLibraryClient());

		return new MeasureEvaluator(measureProvider, libraryProvider, terminologyProvider, dataProviders);
	}

}
