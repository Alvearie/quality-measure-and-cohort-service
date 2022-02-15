/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import com.ibm.cohort.annotations.Generated;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;

/**
 * A purpose-built POJO containing all resolvers necessary for quality
 * measurement artifact resolution.
 */
@Generated
public class R4QualityMeasureResolvers {

    private final FhirResourceResolver<Library> libraryResolver;
    private final FhirResourceResolver<Measure> measureResolver;

    public R4QualityMeasureResolvers(FhirResourceResolver<Library> libraryResolver, FhirResourceResolver<Measure> measureResolver) {
        this.libraryResolver = libraryResolver;
        this.measureResolver = measureResolver;
    }

    public FhirResourceResolver<Library> getLibraryResolver() {
        return libraryResolver;
    }

    public FhirResourceResolver<Measure> getMeasureResolver() {
        return measureResolver;
    }

}
