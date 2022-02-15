/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import org.apache.commons.io.IOUtils;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class R4TranslatingLibraryLoaderTest {

    private static final String NAME = "Test";
    private static final String VERSION = "1.0.0";
    
    @Test
    public void load_validCql() throws IOException {
        Library library = new Library();
        withIdentifiers(library, NAME, VERSION);
        withContent(library, "/cql/basic/Test-1.0.0.cql", "text/cql");

        R4TranslatingLibraryLoader loader = getLoader(getLibraryResolver(NAME, VERSION, library));
        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId(NAME)
                .withVersion(VERSION);
        org.cqframework.cql.elm.execution.Library actual = loader.load(identifier);

        Assert.assertEquals(identifier, actual.getIdentifier());
        Assert.assertEquals(4, actual.getStatements().getDef().size());
    }

    @Test
    public void load_validElm() throws IOException {
        Library library = new Library();
        withIdentifiers(library, NAME, VERSION);
        withContent(library, "/cql/basic/Test-1.0.0.cql", "text/cql");
        withContent(library, "/cql/basic/Test-1.0.0.xml", "application/elm+xml");

        R4TranslatingLibraryLoader loader = getLoader(getLibraryResolver(NAME, VERSION, library));
        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId(NAME)
                .withVersion(VERSION);
        org.cqframework.cql.elm.execution.Library actual = loader.load(identifier);

        Assert.assertEquals(identifier, actual.getIdentifier());
        Assert.assertEquals(4, actual.getStatements().getDef().size());
    }

    @Test
    public void load_validCqlAndElm() throws IOException {
        Library library = new Library();
        withIdentifiers(library, NAME, VERSION);
        withContent(library, "/cql/basic/Test-1.0.0.xml", "application/elm+xml");

        R4TranslatingLibraryLoader loader = getLoader(getLibraryResolver(NAME, VERSION, library));
        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId(NAME)
                .withVersion(VERSION);
        org.cqframework.cql.elm.execution.Library actual = loader.load(identifier);

        Assert.assertEquals(identifier, actual.getIdentifier());
        Assert.assertEquals(4, actual.getStatements().getDef().size());
    }

    @Test
    public void load_noLibraryFound() {
        R4TranslatingLibraryLoader loader = getLoader(getLibraryResolver(NAME, VERSION, null));
        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId(NAME)
                .withVersion(VERSION);

        String expectedPrefix = String.format("Library %s-%s not", NAME, VERSION);
        assertIllegalArgumentException(() -> loader.load(identifier), expectedPrefix);
    }

    @Test
    public void load_nullContentType() throws IOException {
        Library library = new Library();
        withIdentifiers(library, NAME, VERSION);
        withContent(library, "/cql/basic/Test-1.0.0.cql", null);

        R4TranslatingLibraryLoader loader = getLoader(getLibraryResolver(NAME, VERSION, library));
        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId(NAME)
                .withVersion(VERSION);

        String expectedPrefix = String.format("Library %s-%s contains", NAME, VERSION);
        assertIllegalArgumentException(() -> loader.load(identifier), expectedPrefix);
    }

    @Test
    public void load_invalidXmlContent() throws IOException {
        Library library = new Library();
        withIdentifiers(library, NAME, VERSION);
        withContent(library, "/cql/basic/Test-1.0.0.cql", "application/elm+xml");

        R4TranslatingLibraryLoader loader = getLoader(getLibraryResolver(NAME, VERSION, library));
        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId(NAME)
                .withVersion(VERSION);

        String expectedPrefix = String.format("Library %s-%s elm attachment", NAME, VERSION);
        assertIllegalArgumentException(() -> loader.load(identifier), expectedPrefix);
    }

    @Test
    public void load_invalidContentType() throws IOException {
        Library library = new Library();
        withIdentifiers(library, NAME, VERSION);
        withContent(library, "/cql/basic/Test-1.0.0.cql", "text/cobol");

        R4TranslatingLibraryLoader loader = getLoader(getLibraryResolver(NAME, VERSION, library));
        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId(NAME)
                .withVersion(VERSION);

        String expectedPrefix = String.format("Library %s-%s must contain", NAME, VERSION);
        assertIllegalArgumentException(() -> loader.load(identifier), expectedPrefix);
    }

    @Test
    public void load_invalidCqlContent() throws IOException {
        Library library = new Library();
        withIdentifiers(library, NAME, VERSION);
        withContent(library, "/cql/basic/Test-1.0.0.xml", "text/cql");

        R4TranslatingLibraryLoader loader = getLoader(getLibraryResolver(NAME, VERSION, library));
        VersionedIdentifier identifier = new VersionedIdentifier()
                .withId(NAME)
                .withVersion(VERSION);

        String expectedPrefix = String.format("Library %s-%s cql attachment", NAME, VERSION);
        assertIllegalArgumentException(() -> loader.load(identifier), expectedPrefix);
    }

    private void withContent(Library library, String resourcePath, String contentType) throws IOException {
        Attachment attachment = new Attachment();
        attachment.setContentType(contentType);
        attachment.setData(IOUtils.resourceToByteArray(resourcePath));
        library.addContent(attachment);
    }

    private void withIdentifiers(Library library, String name, String version) {
        library.setName(name);
        library.setVersion(version);
    }

    private FhirResourceResolver<Library> getLibraryResolver(String name, String version, Library library) {
        FhirResourceResolver<Library> resolver = Mockito.mock(FhirResourceResolver.class);
        Mockito.when(resolver.resolveByName(name, version))
                .thenReturn(library);
        return resolver;
    }

    private R4TranslatingLibraryLoader getLoader(FhirResourceResolver<Library> resolver) {
        CqlToElmTranslator translator = new CqlToElmTranslator();
        return new R4TranslatingLibraryLoader(resolver, translator);
    }

    private void assertIllegalArgumentException(Runnable runnable, String expectedPrefix) {
        boolean foundException = false;
        try {
            runnable.run();
        }
        catch (IllegalArgumentException iae) {
            if (iae.getMessage().startsWith(expectedPrefix)) {
                foundException = true;
            }
        }
        Assert.assertTrue(foundException);
    }

}
