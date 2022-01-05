/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import com.ibm.cohort.cql.fhir.handler.ResourceHandler;
import com.ibm.cohort.cql.hapi.handler.R4LibraryResourceHandler;
import com.ibm.cohort.cql.version.ResourceSelector;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class R4FhirBundleExtractorTest {

    @Test
    public void extract_happyPath() {
        String expectedVersion = "1.0.0";
        Library library = new Library().setVersion(expectedVersion);

        Bundle bundle = new Bundle();
        addToBundle(library, bundle);

        R4FhirBundleExtractor<Library> extractor = getExtractor();
        Library actual = extractor.extract(bundle, expectedVersion);
        Assert.assertEquals(expectedVersion, actual.getVersion());
    }

    @Test
    public void extract_incompatibleType() {
        String expectedVersion = "1.0.0";
        Measure measure = new Measure().setVersion(expectedVersion);

        Bundle bundle = new Bundle();
        addToBundle(measure, bundle);

        R4FhirBundleExtractor<Library> extractor = getExtractor();
        Library actual = extractor.extract(bundle, expectedVersion);
        Assert.assertNull(actual);
    }

    @Test
    public void extractAll() {
        String firstVersion = "1.0.0";
        String secondVersion = "2.0.0";
        String thirdVersion = "3.0.0";
        String fourthVersion = "4.0.0";

        Library library1 = new Library().setVersion(firstVersion);
        Library library2 = new Library().setVersion(secondVersion);
        Measure measure = new Measure().setVersion(thirdVersion);
        Library library3 = new Library().setVersion(fourthVersion);

        Bundle bundle = new Bundle();
        addToBundle(library1, bundle);
        addToBundle(library2, bundle);
        addToBundle(measure, bundle);
        addToBundle(library3, bundle);

        R4FhirBundleExtractor<Library> extractor = getExtractor();
        List<Library> libraries = extractor.extractAll(bundle);
        List<String> actual = libraries.stream().map(Library::getVersion).collect(Collectors.toList());
        List<String> expected = Arrays.asList(firstVersion, secondVersion, fourthVersion);
        Assert.assertEquals(expected, actual);
    }

    private R4FhirBundleExtractor<Library> getExtractor() {
        ResourceHandler<Library, Identifier> libraryHandler = new R4LibraryResourceHandler();
        ResourceSelector<Library> resourceSelector = new ResourceSelector<>(libraryHandler);
        return new R4FhirBundleExtractor<>(libraryHandler, resourceSelector);
    }

    private void addToBundle(Resource resource, Bundle bundle) {
        BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();
        bundleEntryComponent.setResource(resource);
        bundle.addEntry(bundleEntryComponent);
    }

}
