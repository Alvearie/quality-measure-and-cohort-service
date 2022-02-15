/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.gclient.UriClientParam;
import com.ibm.cohort.cql.hapi.R4FhirBundleExtractor;
import org.hl7.fhir.r4.model.Library;

/**
 * The {@link R4BaseFhirServerResourceResolver} implementation for the {@link Library} type.
 */
public class R4LibraryFhirServerResourceResolver extends R4BaseFhirServerResourceResolver<Library> {

    public R4LibraryFhirServerResourceResolver(IGenericClient client, R4FhirBundleExtractor<Library> bundleExtractor) {
        super(client, bundleExtractor);
    }

    @Override
    protected Class<Library> getResourceClass() {
        return Library.class;
    }

    @Override
    protected StringClientParam getNameParameter() {
        return Library.NAME;
    }

    @Override
    protected UriClientParam getUrlParameter() {
        return Library.URL;
    }

    @Override
    protected TokenClientParam getVersionParameter() {
        return Library.VERSION;
    }

    @Override
    protected TokenClientParam getIdentifierParameter() {
        return Library.IDENTIFIER;
    }

}
