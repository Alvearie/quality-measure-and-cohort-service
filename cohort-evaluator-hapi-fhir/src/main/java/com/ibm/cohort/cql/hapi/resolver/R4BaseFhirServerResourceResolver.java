/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.resolver;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.gclient.UriClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.R4FhirBundleExtractor;
import com.ibm.cohort.cql.helpers.CanonicalHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;

/**
 * A base implementation of a {@link FhirResourceResolver} that queries
 * a FHIR server using the provided {@link IGenericClient}.
 *
 * <p> The {@link T} type is expected to have the same general identifier
 * fields found on most common FHIR knowledge artifacts (e.g. id, name, version, etc).
 *
 * @param <T> The FHIR type to resolve.
 */
public abstract class R4BaseFhirServerResourceResolver<T extends IBaseResource> implements FhirResourceResolver<T> {

    private final IGenericClient client;
    private final R4FhirBundleExtractor<T> bundleExtractor;

    public R4BaseFhirServerResourceResolver(IGenericClient client, R4FhirBundleExtractor<T> bundleExtractor) {
        this.client = client;
        this.bundleExtractor = bundleExtractor;
    }

    protected abstract Class<T> getResourceClass();
    protected abstract StringClientParam getNameParameter();
    protected abstract UriClientParam getUrlParameter();
    protected abstract TokenClientParam getVersionParameter();
    protected abstract TokenClientParam getIdentifierParameter();

    @Override
    public T resolveById(String id) {
        T retVal;
        try {
            retVal = client.read()
                    .resource(getResourceClass())
                    .withId(id)
                    .execute();
        }
        catch (ResourceNotFoundException rnfe) {
            retVal = null;
        }
        return retVal;
    }

    @Override
    public T resolveByName(String name, String version) {
        IQuery<IBaseBundle> query = client.search()
                .forResource(getResourceClass())
                .where(getNameParameter().matchesExactly().value(name));
        Bundle bundle = withOptionalVersion(query, version)
                .returnBundle(Bundle.class)
                .execute();
        return bundleExtractor.extract(bundle, version);
    }

    @Override
    public T resolveByCanonicalUrl(String canonicalUrl) {
        Pair<String, String> splitUrl = CanonicalHelper.separateParts(canonicalUrl);
        IQuery<IBaseBundle> query = client.search()
                .forResource(getResourceClass())
                .where(getUrlParameter().matches().value(splitUrl.getLeft()));
        Bundle bundle = withOptionalVersion(query, splitUrl.getRight())
                .returnBundle(Bundle.class)
                .execute();
        return bundleExtractor.extract(bundle, splitUrl.getRight());
    }

    @Override
    public T resolveByIdentifier(String value, String system, String version) {
        IQuery<IBaseBundle> query = client.search()
                .forResource(getResourceClass())
                .where(getIdentifierParameter().exactly().systemAndValues(system, value));
        Bundle bundle = withOptionalVersion(query, version)
                .returnBundle(Bundle.class)
                .execute();
        return bundleExtractor.extract(bundle, version);
    }

    private IQuery<IBaseBundle> withOptionalVersion(IQuery<IBaseBundle> query, String version) {
        return StringUtils.isEmpty(version) ? query : query.and(getVersionParameter().exactly().code(version));
    }

}
