/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.handler;

import com.ibm.cohort.cql.fhir.handler.ResourceHandler;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;

import java.util.List;

/**
 * The R4 HAPI FHIR {@link Library} implementation of {@link ResourceHandler}.
 */
public class R4LibraryResourceHandler implements ResourceHandler<Library, Identifier> {

    @Override
    public Class<Library> getSupportedClass() {
        return Library.class;
    }

    @Override
    public String getId(Library resource) {
        return resource.getId();
    }

    @Override
    public void setId(String id, Library resource) {
        IdType idType = new IdType(resource.fhirType(), id);
        resource.setId(idType);
    }

    @Override
    public String getName(Library resource) {
        return resource.getName();
    }

    @Override
    public String getVersion(Library resource) {
        return resource.getVersion();
    }

    @Override
    public String getUrl(Library resource) {
        return resource.getUrl();
    }

    @Override
    public List<Identifier> getIdentifiers(Library resource) {
        return resource.getIdentifier();
    }

    @Override
    public String getIdentifierValue(Identifier identifier) {
        return identifier.getValue();
    }

    @Override
    public String getIdentifierSystem(Identifier identifier) {
        return identifier.getSystem();
    }

}
