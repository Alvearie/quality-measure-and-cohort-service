/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.translator.provider;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.bind.JAXB;

import org.hl7.elm_modelinfo.r1.ModelInfo;

public class ModelInfoReader {
    public static ModelInfo convertToModelInfo(InputStream modelInfoInputStream) {
        return JAXB.unmarshal(modelInfoInputStream, ModelInfo.class);
    }

    public static ModelInfo convertToModelInfo(Reader modelInfoReader) {
        return JAXB.unmarshal(modelInfoReader, ModelInfo.class);
    }

    public static ModelInfo convertToModelInfo(File modelInfoFile) {
        return JAXB.unmarshal(modelInfoFile, ModelInfo.class);
    }
}
