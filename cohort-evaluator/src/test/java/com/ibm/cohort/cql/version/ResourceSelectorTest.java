/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.version;

import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import com.ibm.cohort.cql.testmodel.SimpleIdentifier;
import com.ibm.cohort.cql.testmodel.SimpleObject;
import com.ibm.cohort.cql.testmodel.SimpleResourceFieldHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResourceSelectorTest {

    @Test
    public void selectSpecificVersionOrLatest_specificVersion() {
        String id = "3";
        String version = "1.0.3";
        SimpleObject expected = createObject(id, version);

        List<SimpleObject> objects = Arrays.asList(
                createObject("0", "1.0.0"),
                createObject("1", "1.0.1"),
                createObject("2", "1.0.2"),
                expected,
                createObject("4", "1.0.4"),
                createObject("5", "1.0.5")
        );

        ResourceSelector<SimpleObject> selector = getSelector();
        SimpleObject actual = selector.selectSpecificVersionOrLatest(objects, version);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void selectSpecificVersionOrLatest_noVersion() {
        String id = "5";
        String version = "1.0.5";
        SimpleObject expected = createObject(id, version);

        List<SimpleObject> objects = Arrays.asList(
                createObject("0", "1.0.0"),
                createObject("1", "1.0.1"),
                createObject("2", "1.0.2"),
                createObject("3", "1.0.3"),
                createObject("4", "1.0.4"),
                expected
        );

        ResourceSelector<SimpleObject> selector = getSelector();
        SimpleObject actual = selector.selectSpecificVersionOrLatest(objects, null);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void selectSpecificVersionOrLatest_versionNotFound() {
        List<SimpleObject> objects = Arrays.asList(
                createObject("0", "1.0.0"),
                createObject("1", "1.0.1"),
                createObject("2", "1.0.2"),
                createObject("3", "1.0.3"),
                createObject("4", "1.0.4"),
                createObject("5", "1.0.5")
        );

        ResourceSelector<SimpleObject> selector = getSelector();
        SimpleObject actual = selector.selectSpecificVersionOrLatest(objects, "9.9.99");
        Assert.assertNull(actual);
    }

    @Test
    public void selectSpecificVersionOrLatest_noObjects() {
        List<SimpleObject> objects = Collections.emptyList();
        ResourceSelector<SimpleObject> selector = getSelector();
        SimpleObject actual = selector.selectSpecificVersionOrLatest(objects, "1.0.0");
        Assert.assertNull(actual);
    }

    @Test
    public void selectSpecificVersionOrLatest_specificVersion_duplicateVersions() {
        String id = "3";
        String version = "1.0.3";
        SimpleObject expected = createObject(id, version);

        List<SimpleObject> objects = Arrays.asList(
                createObject("0", "1.0.0"),
                createObject("1", version),
                createObject("2", "1.0.2"),
                expected,
                createObject("4", "1.0.4"),
                createObject("5", "1.0.5")
        );

        ResourceSelector<SimpleObject> selector = getSelector();
        Assert.assertThrows(
                IllegalArgumentException.class,
                () -> selector.selectSpecificVersionOrLatest(objects, version)
        );
    }

    /*
     * I'm not sure if this is desirable behavior since duplicates cause an
     * exception when specifying a specific verison.
     */
    @Test
    public void selectSpecificVersionOrLatest_noVersion_duplicateVersions() {
        String id = "2";
        String version = "1.0.5";
        SimpleObject expected = createObject(id, version);

        List<SimpleObject> objects = Arrays.asList(
                createObject("0", "1.0.0"),
                createObject("1", "1.0.1"),
                expected,
                createObject("3", version),
                createObject("4", "1.0.4"),
                createObject("5", version)

        );

        ResourceSelector<SimpleObject> selector = getSelector();
        SimpleObject actual = selector.selectSpecificVersionOrLatest(objects, null);
        Assert.assertEquals(expected, actual);
    }

    private ResourceSelector<SimpleObject> getSelector() {
        ResourceFieldHandler<SimpleObject, SimpleIdentifier> fieldHandler = new SimpleResourceFieldHandler();
        return new ResourceSelector<>(fieldHandler);
    }

    private SimpleObject createObject(String id, String version) {
        SimpleObject retVal = new SimpleObject();
        retVal.setId(id);
        retVal.setVersion(version);
        return retVal;
    }

}
