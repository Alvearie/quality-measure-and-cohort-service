/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.testmodel;

import com.ibm.cohort.annotations.Generated;
import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;

import java.util.List;

/**
 * An implementation of {@link ResourceFieldHandler} that operates on
 * {@link SimpleObject} resources.
 * Used for assisting in testing objects that require a {@link ResourceFieldHandler}
 * implementation.
 */
@Generated
public class SimpleResourceFieldHandler implements ResourceFieldHandler<SimpleObject, SimpleIdentifier> {

    @Override
    public Class<SimpleObject> getSupportedClass() {
        return SimpleObject.class;
    }

    @Override
    public String getVersion(SimpleObject resource) {
        return resource.getVersion();
    }

    @Override
    public String getId(SimpleObject resource) {
        return resource.getId();
    }

    @Override
    public void setId(String id, SimpleObject resource) {
        resource.setId(id);
    }

    @Override
    public String getName(SimpleObject resource) {
        return resource.getName();
    }

    @Override
    public String getUrl(SimpleObject resource) {
        return resource.getUrl();
    }

    @Override
    public List<SimpleIdentifier> getIdentifiers(SimpleObject resource) {
        return resource.getIdentifiers();
    }

    @Override
    public String getIdentifierValue(SimpleIdentifier identifier) {
        return identifier.getValue();
    }

    @Override
    public String getIdentifierSystem(SimpleIdentifier identifier) {
        return identifier.getSystem();
    }

}
