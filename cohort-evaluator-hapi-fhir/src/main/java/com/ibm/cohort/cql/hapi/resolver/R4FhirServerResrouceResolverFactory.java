/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.ibm.cohort.cql.fhir.resolver.CachingFhirResourceResolver;
import com.ibm.cohort.cql.version.ResourceSelector;
import com.ibm.cohort.cql.fhir.handler.ResourceHandler;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.R4FhirBundleExtractor;
import com.ibm.cohort.cql.hapi.handler.R4LibraryResourceHandler;
import com.ibm.cohort.cql.hapi.handler.R4MeasureResourceHandler;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;

/**
 * A set of factory methods for more easily creating {@link FhirResourceResolver}
 * instances that query a FHIR server.
 */
public class R4FhirServerResrouceResolverFactory {

    public static FhirResourceResolver<Measure> createMeasureResolver(IGenericClient measureClient) {
        ResourceHandler<Measure, Identifier> measureHandler = new R4MeasureResourceHandler();
        ResourceSelector<Measure> measureSelector = new ResourceSelector<>(measureHandler);
        R4FhirBundleExtractor<Measure> measureExtractor = new R4FhirBundleExtractor<>(measureHandler, measureSelector);
        FhirResourceResolver<Measure> measureResolver = new R4MeasureFhirServerResourceResolver(measureClient, measureExtractor);
        return new CachingFhirResourceResolver<>(measureResolver);
    }

    public static FhirResourceResolver<Library> createLibraryResolver(IGenericClient libraryClient) {
        ResourceHandler<Library, Identifier> libraryHandler = new R4LibraryResourceHandler();
        ResourceSelector<Library> librarySelector = new ResourceSelector<>(libraryHandler);
        R4FhirBundleExtractor<Library> libraryExtractor = new R4FhirBundleExtractor<>(libraryHandler, librarySelector);
        FhirResourceResolver<Library> libraryResolver = new R4LibraryFhirServerResourceResolver(libraryClient, libraryExtractor);
        return new CachingFhirResourceResolver<>(libraryResolver);
    }

}
