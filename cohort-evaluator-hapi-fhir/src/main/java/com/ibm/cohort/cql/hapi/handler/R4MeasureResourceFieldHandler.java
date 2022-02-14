/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi.handler;

import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Measure;

import java.util.List;

/**
 * The R4 HAPI FHIR {@link Measure} implementation of {@link ResourceFieldHandler}.
 */
public class R4MeasureResourceFieldHandler implements ResourceFieldHandler<Measure, Identifier> {

    @Override
    public Class<Measure> getSupportedClass() {
        return Measure.class;
    }

    @Override
    public String getId(Measure resource) {
        return resource.getId();
    }

    @Override
    public void setId(String id, Measure resource) {
        IdType idType = new IdType(resource.fhirType(), id);
        resource.setId(idType);
    }

    @Override
    public String getName(Measure resource) {
        return resource.getName();
    }

    @Override
    public String getVersion(Measure resource) {
        return resource.getVersion();
    }

    @Override
    public String getUrl(Measure resource) {
        return resource.getUrl();
    }

    @Override
    public List<Identifier> getIdentifiers(Measure resource) {
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
