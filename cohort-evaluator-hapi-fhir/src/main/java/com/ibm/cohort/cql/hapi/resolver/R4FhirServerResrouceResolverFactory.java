/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.ibm.cohort.cql.fhir.resolver.CachingFhirResourceResolver;
import com.ibm.cohort.cql.version.ResourceSelector;
import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.R4FhirBundleExtractor;
import com.ibm.cohort.cql.hapi.handler.R4LibraryResourceFieldHandler;
import com.ibm.cohort.cql.hapi.handler.R4MeasureResourceFieldHandler;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;

/**
 * A set of factory methods for more easily creating {@link FhirResourceResolver}
 * instances that query a FHIR server.
 */
public class R4FhirServerResrouceResolverFactory {

    public static FhirResourceResolver<Measure> createMeasureResolver(IGenericClient measureClient) {
        ResourceFieldHandler<Measure, Identifier> measureFieldHandler = new R4MeasureResourceFieldHandler();
        ResourceSelector<Measure> measureSelector = new ResourceSelector<>(measureFieldHandler);
        R4FhirBundleExtractor<Measure> measureExtractor = new R4FhirBundleExtractor<>(measureFieldHandler, measureSelector);
        FhirResourceResolver<Measure> measureResolver = new R4MeasureFhirServerResourceResolver(measureClient, measureExtractor);
        return new CachingFhirResourceResolver<>(measureResolver);
    }

    public static FhirResourceResolver<Library> createLibraryResolver(IGenericClient libraryClient) {
        ResourceFieldHandler<Library, Identifier> libraryFieldHandler = new R4LibraryResourceFieldHandler();
        ResourceSelector<Library> librarySelector = new ResourceSelector<>(libraryFieldHandler);
        R4FhirBundleExtractor<Library> libraryExtractor = new R4FhirBundleExtractor<>(libraryFieldHandler, librarySelector);
        FhirResourceResolver<Library> libraryResolver = new R4LibraryFhirServerResourceResolver(libraryClient, libraryExtractor);
        return new CachingFhirResourceResolver<>(libraryResolver);
    }

}
