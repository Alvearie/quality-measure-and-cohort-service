/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.fhir.resolver;

import com.ibm.cohort.cql.testmodel.SimpleIdentifier;
import com.ibm.cohort.cql.testmodel.SimpleObject;
import com.ibm.cohort.cql.testmodel.SimpleResourceFieldHandler;
import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import com.ibm.cohort.cql.version.ResourceSelector;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapFhirResourceResolverTest {

    @Test
    public void resolveById() {
        String shortId = "id";
        String fullId = "full/" + shortId;
        SimpleObject expected = new SimpleObject(
                fullId,
                "1.0.0",
                "name",
                "url",
                Collections.emptyList()
        );

        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        resolver.addResource(expected);

        Assert.assertEquals(expected, resolver.resolveById(shortId));
        Assert.assertEquals(expected, resolver.resolveById(fullId));
    }

    @Test
    public void resolveById_usingShortId() {
        String id = "id";
        SimpleObject expected = new SimpleObject(
                id,
                "1.0.0",
                "name",
                "url",
                Collections.emptyList()
        );

        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        resolver.addResource(expected);

        Assert.assertEquals(expected, resolver.resolveById(id));
    }

    @Test
    public void resolveById_notFound() {
        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        Assert.assertNull(resolver.resolveById("id"));
    }

    @Test
    public void resolveByName() {
        String name = "name";
        String version = "1.0.0";
        SimpleObject expected = new SimpleObject(
                "full/id",
                version,
                name,
                "url",
                Collections.emptyList()
        );

        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        resolver.addResource(expected);

        Assert.assertEquals(expected, resolver.resolveByName(name, version));
    }

    @Test
    public void resolveByName_notFound() {
        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        Assert.assertNull(resolver.resolveByName("name", "1.0.0"));
    }

    @Test
    public void resolveByCanonicalUrl() {
        String url = "url";
        String version = "1.0.0";
        String canonicalUrl = url + "|" + version;
        SimpleObject expected = new SimpleObject(
                "full/id",
                version,
                "name",
                url,
                Collections.emptyList()
        );

        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        resolver.addResource(expected);

        Assert.assertEquals(expected, resolver.resolveByCanonicalUrl(canonicalUrl));
    }

    @Test
    public void resolveByCanonicalUrl_notFound() {
        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        Assert.assertNull(resolver.resolveByCanonicalUrl("url|1.0.0"));
    }

    @Test
    public void resolveByIdentifier() {
        String version = "1.0.0";

        SimpleIdentifier identifier1 = new SimpleIdentifier("value1", "system1");
        SimpleIdentifier identifier2 = new SimpleIdentifier("value2", "system2");
        SimpleIdentifier identifier3 = new SimpleIdentifier("value3", "system3");

        SimpleObject expected = new SimpleObject(
                "full/id",
                version,
                "name",
                "url",
                Arrays.asList(identifier1, identifier2, identifier3)
        );

        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        resolver.addResource(expected);

        Assert.assertEquals(expected, resolver.resolveByIdentifier(identifier1.getValue(), identifier1.getSystem(), version));
        Assert.assertEquals(expected, resolver.resolveByIdentifier(identifier2.getValue(), identifier2.getSystem(), version));
        Assert.assertEquals(expected, resolver.resolveByIdentifier(identifier3.getValue(), identifier3.getSystem(), version));

        Assert.assertNull(resolver.resolveByIdentifier(identifier1.getValue(), identifier2.getSystem(), version));
        Assert.assertNull(resolver.resolveByIdentifier(identifier3.getValue(), identifier1.getSystem(), version));
    }

    @Test
    public void resolveByIdentifier_notFound() {
        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        Assert.assertNull(resolver.resolveByIdentifier("value", "system", "1.0.0"));
    }

    @Test
    public void multipleObjectsMixedTest() {
        SimpleObject expected1 = new SimpleObject(
                "full/id1",
                "1.0.1",
                "name1",
                "url1",
                Arrays.asList(
                        new SimpleIdentifier("value11", "system11"),
                        new SimpleIdentifier("value12", "system12"),
                        new SimpleIdentifier("value13", "system13")
                )
        );
        SimpleObject expected2 = new SimpleObject(
                "full/id2",
                "1.0.2",
                "name2",
                "url2",
                Arrays.asList(
                        new SimpleIdentifier("value21", "system21"),
                        new SimpleIdentifier("value22", "system22"),
                        new SimpleIdentifier("value23", "system23")
                )
        );
        SimpleObject expected3 = new SimpleObject(
                "full/id3",
                "1.0.3",
                "name3",
                "url3",
                Arrays.asList(
                        new SimpleIdentifier("value31", "system31"),
                        new SimpleIdentifier("value32", "system32"),
                        new SimpleIdentifier("value33", "system33")
                )
        );

        List<SimpleObject> objects = Arrays.asList(expected1, expected2, expected3);
        MapFhirResourceResolver<SimpleObject, SimpleIdentifier> resolver = getResolver();
        resolver.addResources(objects);

        for (SimpleObject object : objects) {
            Assert.assertEquals(object, resolver.resolveById(object.getId()));
            Assert.assertEquals(object, resolver.resolveByName(object.getName(), object.getVersion()));
            Assert.assertEquals(object, resolver.resolveByCanonicalUrl(object.getUrl() + "|" + object.getVersion()));

            for (SimpleIdentifier identifier: object.getIdentifiers()) {
                Assert.assertEquals(object, resolver.resolveByIdentifier(identifier.getValue(), identifier.getSystem(), object.getVersion()));
            }
        }
    }

    private MapFhirResourceResolver<SimpleObject, SimpleIdentifier> getResolver() {
        ResourceFieldHandler<SimpleObject, SimpleIdentifier> fieldHandler = new SimpleResourceFieldHandler();
        ResourceSelector<SimpleObject> resourceSelector = new ResourceSelector<>(fieldHandler);
        return new MapFhirResourceResolver<>(fieldHandler, resourceSelector);
    }

}
