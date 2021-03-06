/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.translation;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.elm_modelinfo.r1.ModelInfo;

import com.ibm.cohort.engine.LibraryFormat;

/**
 * A general interface that can be used to abstract interaction with multiple
 * implementations of a CqlTranslator such as linking directly to the translator
 * JAR or calling out to the CqlTranslationService microservice.
 */
public interface CqlTranslationProvider {
	Library translate(InputStream cql) throws Exception;

	Library translate(InputStream cql, List<Options> options) throws Exception;

	Library translate(InputStream cql, List<Options> options, LibraryFormat targetFormat) throws Exception;
	
	void registerModelInfo(ModelInfo modelInfo);
	
	default void convertAndRegisterModelInfo(InputStream modelInfoInputStream) {
		registerModelInfo(convertToModelInfo(modelInfoInputStream));
	}

	default void convertAndRegisterModelInfo(File modelInfoFile) {
		registerModelInfo(convertToModelInfo(modelInfoFile));
	}

	default void convertAndRegisterModelInfo(Reader modelInfoReader) {
		registerModelInfo(convertToModelInfo(modelInfoReader));
	}

	ModelInfo convertToModelInfo(InputStream modelInfoInputStream);

	ModelInfo convertToModelInfo(File modelInfoFile);

	ModelInfo convertToModelInfo(Reader modelInfoReader);
}
