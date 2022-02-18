/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.cqframework.cql.cql2elm.ModelInfoProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.elm_modelinfo.r1.ModelInfo;

public class CustomModelInfoProvider implements ModelInfoProvider {

    private Map<VersionedIdentifier,ModelInfo> models = new HashMap<>();
    
    public void addModel(Reader modelInfoXML) {
        ModelInfo modelInfo = JAXB.unmarshal(modelInfoXML, ModelInfo.class);
        addModel(modelInfo);
    }
    
    public void addModel(File modelInfoXML) {
        ModelInfo modelInfo = JAXB.unmarshal(modelInfoXML, ModelInfo.class);
        addModel(modelInfo);
    }

    protected void addModel(ModelInfo modelInfo) {
        VersionedIdentifier modelId = new VersionedIdentifier().withId(modelInfo.getName())
                .withVersion(modelInfo.getVersion());
        models.put(modelId, modelInfo);
    }
    
    @Override
    public ModelInfo load(VersionedIdentifier modelIdentifier) {
        return models.get(modelIdentifier);
    }

    public Map<VersionedIdentifier, ModelInfo> getModels() {
        return models;
    }
}
