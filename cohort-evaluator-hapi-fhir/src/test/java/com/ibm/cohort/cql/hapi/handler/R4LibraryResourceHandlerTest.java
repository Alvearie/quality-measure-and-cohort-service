/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.handler;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class R4LibraryResourceHandlerTest {

    private final R4LibraryResourceHandler resourceHandler = new R4LibraryResourceHandler();

    @Test
    public void getSupportedClass() {
        Assert.assertEquals(Library.class, resourceHandler.getSupportedClass());
    }

    @Test
    public void getId() {
        String id = "id";
        Library library = new Library();
        library.setId(id);
        Assert.assertEquals(id, resourceHandler.getId(library));
    }

    @Test
    public void setId() {
        String id = "id";
        Library library = new Library();
        resourceHandler.setId(id, library);
        Assert.assertEquals("Library/" + id, resourceHandler.getId(library));
    }

    @Test
    public void getName() {
        String name = "name";
        Library library = new Library();
        library.setName(name);
        Assert.assertEquals(name, resourceHandler.getName(library));
    }

    @Test
    public void getVersion() {
        String version = "version";
        Library library = new Library();
        library.setVersion(version);
        Assert.assertEquals(version, resourceHandler.getVersion(library));
    }

    @Test
    public void getUrl() {
        String url = "url";
        Library library = new Library();
        library.setUrl(url);
        Assert.assertEquals(url, resourceHandler.getUrl(library));
    }

    @Test
    public void getIdentifiers() {
        Identifier identifier = new Identifier()
                .setSystem("system")
                .setValue("value");
        Library library = new Library();
        library.addIdentifier(identifier);
        List<Identifier> libraryIdentifiers = resourceHandler.getIdentifiers(library);
        Assert.assertEquals(1, libraryIdentifiers.size());
        Assert.assertTrue(identifier.equalsDeep(libraryIdentifiers.get(0)));
    }

    @Test
    public void getIdentifierValue() {
        String identifierValue = "value";
        Identifier identifier = new Identifier()
                .setValue(identifierValue);
        Assert.assertEquals(identifierValue, resourceHandler.getIdentifierValue(identifier));
    }

    @Test
    public void getIdentifierSystem() {
        String identifierSystem = "system";
        Identifier identifier = new Identifier()
                .setSystem(identifierSystem);
        Assert.assertEquals(identifierSystem, resourceHandler.getIdentifierSystem(identifier));
    }

}
