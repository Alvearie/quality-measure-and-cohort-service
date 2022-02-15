/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import com.ibm.cohort.cql.version.ResourceSelector;
import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Identifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Extracts all instances of a specific resource type from a FHIR {@link Bundle}.
 *
 * @param <T> The FHIR resource type to extract from a {@link Bundle}
 */
public class R4FhirBundleExtractor<T extends IBaseResource> {

    private final ResourceFieldHandler<T, Identifier> fieldHandler;
    private final ResourceSelector<T> resourceSelector;

    public R4FhirBundleExtractor(ResourceFieldHandler<T, Identifier> fieldHandler, ResourceSelector<T> resourceSelector) {
        this.fieldHandler = fieldHandler;
        this.resourceSelector = resourceSelector;
    }

    public T extract(Bundle bundle, String version) {
        List<T> resources = extractAll(bundle);
        return resourceSelector.selectSpecificVersionOrLatest(resources, version);
    }

    public List<T> extractAll(Bundle bundle) {
        Class<T> supportedClass = fieldHandler.getSupportedClass();
        return bundle.getEntry().stream()
                .map(BundleEntryComponent::getResource)
                .filter(supportedClass::isInstance)
                .map(supportedClass::cast)
                .collect(Collectors.toList());
    }

}
