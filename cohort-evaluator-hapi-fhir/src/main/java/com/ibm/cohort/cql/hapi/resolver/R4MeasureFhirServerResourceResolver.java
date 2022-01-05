/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.gclient.UriClientParam;
import com.ibm.cohort.cql.hapi.R4FhirBundleExtractor;
import org.hl7.fhir.r4.model.Measure;

/**
 * The {@link R4BaseFhirServerResourceResolver} implementation for the {@link Measure} type.
 */
public class R4MeasureFhirServerResourceResolver extends R4BaseFhirServerResourceResolver<Measure> {

    public R4MeasureFhirServerResourceResolver(IGenericClient client, R4FhirBundleExtractor<Measure> bundleExtractor) {
        super(client, bundleExtractor);
    }

    @Override
    protected Class<Measure> getResourceClass() {
        return Measure.class;
    }

    @Override
    protected StringClientParam getNameParameter() {
        return Measure.NAME;
    }

    @Override
    protected UriClientParam getUrlParameter() {
        return Measure.URL;
    }

    @Override
    protected TokenClientParam getVersionParameter() {
        return Measure.VERSION;
    }

    @Override
    protected TokenClientParam getIdentifierParameter() {
        return Measure.IDENTIFIER;
    }

}
