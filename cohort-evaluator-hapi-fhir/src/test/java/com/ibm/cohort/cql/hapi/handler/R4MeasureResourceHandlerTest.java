/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.handler;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class R4MeasureResourceHandlerTest {

    private final R4MeasureResourceHandler resourceHandler = new R4MeasureResourceHandler();
    
    @Test
    public void getSupportedClass() {
        Assert.assertEquals(Measure.class, resourceHandler.getSupportedClass());
    }

    @Test
    public void getId() {
        String id = "id";
        Measure measure = new Measure();
        measure.setId(id);
        Assert.assertEquals(id, resourceHandler.getId(measure));
    }

    @Test
    public void setId() {
        String id = "id";
        Measure measure = new Measure();
        resourceHandler.setId(id, measure);
        Assert.assertEquals("Measure/" + id, resourceHandler.getId(measure));
    }

    @Test
    public void getName() {
        String name = "name";
        Measure measure = new Measure();
        measure.setName(name);
        Assert.assertEquals(name, resourceHandler.getName(measure));
    }

    @Test
    public void getVersion() {
        String version = "version";
        Measure measure = new Measure();
        measure.setVersion(version);
        Assert.assertEquals(version, resourceHandler.getVersion(measure));
    }

    @Test
    public void getUrl() {
        String url = "url";
        Measure measure = new Measure();
        measure.setUrl(url);
        Assert.assertEquals(url, resourceHandler.getUrl(measure));
    }

    @Test
    public void getIdentifiers() {
        Identifier identifier = new Identifier()
                .setSystem("system")
                .setValue("value");
        Measure measure = new Measure();
        measure.addIdentifier(identifier);
        List<Identifier> measureIdentifiers = resourceHandler.getIdentifiers(measure);
        Assert.assertEquals(1, measureIdentifiers.size());
        Assert.assertTrue(identifier.equalsDeep(measureIdentifiers.get(0)));
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
