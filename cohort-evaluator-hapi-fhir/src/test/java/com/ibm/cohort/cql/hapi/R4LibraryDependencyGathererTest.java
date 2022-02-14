/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.hapi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ibm.cohort.cql.helpers.CanonicalHelper;
import com.ibm.cohort.cql.version.ResourceSelector;
import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import com.ibm.cohort.cql.fhir.resolver.MapFhirResourceResolver;
import com.ibm.cohort.cql.hapi.handler.R4LibraryResourceFieldHandler;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.junit.Assert;
import org.junit.Test;

public class R4LibraryDependencyGathererTest {

    private static final String VERSION_ONE = "1.0.0";

    @Test
    public void gatherForMeasure_happyPath() {
        String firstLibraryDependencyUrl = "http://some.other.url/resources/Library/dependencies/FirstLibraryDependency";
        Library firstLibraryDependency = new Library();
        withIdentifiers(firstLibraryDependency, "firstLibraryDependency", firstLibraryDependencyUrl, VERSION_ONE);
        withContent(firstLibraryDependency, "application/elm+xml");

        String firstLibraryUrl = "http://some.url.com/Library/FirstLibrary";
        String dependencyCanonicalUrl = CanonicalHelper.toCanonicalUrl(firstLibraryDependencyUrl, VERSION_ONE);
        Library firstLibrary = new Library();
        withIdentifiers(firstLibrary, "firstLibrary", firstLibraryUrl, VERSION_ONE);
        withContent(firstLibrary, "application/elm+json");
        withRelation(firstLibrary, dependencyCanonicalUrl);

        String secondLibraryId = "secondLibrary";
        String secondLibraryUrl = "Library/" + secondLibraryId;
        Library secondLibrary = new Library();
        withIdentifiers(secondLibrary, secondLibraryId, secondLibraryUrl, VERSION_ONE);
        asLogicLibrary(secondLibrary);

        Measure measure = createMeasure(
                CanonicalHelper.toCanonicalUrl(firstLibraryUrl, VERSION_ONE),
                secondLibraryUrl,
                "http://some.url.com/InvalidUrl|1.0.0"
        );

        MapFhirResourceResolver<Library, Identifier> resourceResolver = getMapFhirResourceResolver();
        resourceResolver.addResource(firstLibrary);
        resourceResolver.addResource(firstLibraryDependency);
        resourceResolver.addResource(secondLibrary);

        R4LibraryDependencyGatherer dependencyGatherer = new R4LibraryDependencyGatherer(resourceResolver);
        List<Library> libraries = dependencyGatherer.gatherForMeasure(measure);

        List<String> actual = libraries.stream().map(Library::getUrl).collect(Collectors.toList());
        List<String> expected = Arrays.asList(firstLibraryUrl, firstLibraryDependencyUrl, secondLibraryUrl);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void graph_with_cycles___no_infinite_loop() {
        String parentId = "Parent";
        String childId = "Child";

        Library parent = new Library();
        withIdentifiers(parent, parentId, "Parent", "1");
        asLogicLibrary(parent);
        withRelation(parent, "Library/" + childId);

        Library child = new Library();
        withIdentifiers(child, childId, "Child", "1");
        asLogicLibrary(child);
        withRelation(child, "Library/" + parentId);

        MapFhirResourceResolver<Library, Identifier> resourceResolver = getMapFhirResourceResolver();
        resourceResolver.addResource(parent);
        resourceResolver.addResource(child);

        R4LibraryDependencyGatherer dependencyGatherer = new R4LibraryDependencyGatherer(resourceResolver);
        List<Library> libraries = dependencyGatherer.gatherForLibraryId(parentId);
        List<String> actual = libraries.stream().map(Library::getId).collect(Collectors.toList());

        List<String> expected = Arrays.asList(parentId, childId);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void library_with_no_type_but_cql_attachment___returns_library() {
        String libraryId = "lib";

        Library lib = new Library();
        withIdentifiers(lib, libraryId, null, null);
        withContent(lib, "text/cql");

        MapFhirResourceResolver<Library, Identifier> resourceResolver = getMapFhirResourceResolver();
        resourceResolver.addResource(lib);

        R4LibraryDependencyGatherer dependencyGatherer = new R4LibraryDependencyGatherer(resourceResolver);
        List<Library> libraries = dependencyGatherer.gatherForLibraryId(libraryId);
        List<String> actual = libraries.stream().map(Library::getId).collect(Collectors.toList());

        List<String> expected = Collections.singletonList(libraryId);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void library_with_no_valid_data___returns_nothing() {
        String libraryId = "lib";
        Library lib = new Library();
        withIdentifiers(lib, libraryId, null, null);

        MapFhirResourceResolver<Library, Identifier> resourceResolver = getMapFhirResourceResolver();
        resourceResolver.addResource(lib);

        R4LibraryDependencyGatherer dependencyGatherer = new R4LibraryDependencyGatherer(resourceResolver);
        List<Library> libraries = dependencyGatherer.gatherForLibraryId(libraryId);
        Assert.assertTrue(libraries.isEmpty());
    }

    @Test
    public void library_with_type_but_no_coding__returns_nothing() {
        String libraryId = "lib";
        Library lib = new Library();
        withIdentifiers(lib, libraryId, null, null);
        withCodelessType(lib);

        MapFhirResourceResolver<Library, Identifier> resourceResolver = getMapFhirResourceResolver();
        resourceResolver.addResource(lib);

        R4LibraryDependencyGatherer dependencyGatherer = new R4LibraryDependencyGatherer(resourceResolver);
        List<Library> libraries = dependencyGatherer.gatherForLibraryId(libraryId);
        Assert.assertTrue(libraries.isEmpty());
    }

    @Test
    public void library_with_invalid_content__returns_nothing() {
        String libraryId = "lib";
        Library lib = new Library();
        withIdentifiers(lib, libraryId, null, null);
        withContent(lib, "text/cobol");

        MapFhirResourceResolver<Library, Identifier> resourceResolver = getMapFhirResourceResolver();
        resourceResolver.addResource(lib);

        R4LibraryDependencyGatherer dependencyGatherer = new R4LibraryDependencyGatherer(resourceResolver);
        List<Library> libraries = dependencyGatherer.gatherForLibraryId(libraryId);
        Assert.assertTrue(libraries.isEmpty());
    }

    @Test
    public void library_with_invalid_type__returns_nothing() {
        String libraryId = "lib";
        Library lib = new Library();
        withIdentifiers(lib, libraryId, null, null);
        withInvalidType(lib);

        MapFhirResourceResolver<Library, Identifier> resourceResolver = getMapFhirResourceResolver();
        resourceResolver.addResource(lib);

        R4LibraryDependencyGatherer dependencyGatherer = new R4LibraryDependencyGatherer(resourceResolver);
        List<Library> libraries = dependencyGatherer.gatherForLibraryId(libraryId);
        Assert.assertTrue(libraries.isEmpty());
    }

    private void withRelation(Library library, String relatedUrl) {
        RelatedArtifact relation = new RelatedArtifact()
                .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                .setResource(relatedUrl);
        library.addRelatedArtifact(relation);
    }

    private void withIdentifiers(Library library, String id, String url, String version) {
        library.setId(id);
        library.setUrl(url);
        library.setVersion(version);
    }

    private void withContent(Library library, String contentType) {
        Attachment attachment = new Attachment();
        attachment.setContentType(contentType);
        library.addContent(attachment);
    }

    private void asLogicLibrary(Library library) {
        Coding coding = new Coding()
                .setSystem(HapiUtils.CODE_SYSTEM_LIBRARY_TYPE)
                .setCode(HapiUtils.CODE_LOGIC_LIBRARY);
        library.getType().addCoding(coding);
    }

    private void withCodelessType(Library library) {
        CodeableConcept type = new CodeableConcept();
        type.setText("text");
        library.setType(type);
    }

    private void withInvalidType(Library library) {
        Coding coding = new Coding()
                .setSystem("Invalid")
                .setCode("Type");
        library.getType().addCoding(coding);
    }

    private Measure createMeasure(String... libraryCanonicalUrls) {
        Measure measure = new Measure();
        for (String libraryCanonicalUrl : libraryCanonicalUrls) {
            measure.addLibrary(libraryCanonicalUrl);
        }
        return measure;
    }

    private MapFhirResourceResolver<Library, Identifier> getMapFhirResourceResolver() {
        ResourceFieldHandler<Library, Identifier> libraryFieldHandler = new R4LibraryResourceFieldHandler();
        ResourceSelector<Library> resourceSelector = new ResourceSelector<>(libraryFieldHandler);
        return new MapFhirResourceResolver<>(libraryFieldHandler, resourceSelector);
    }

}
