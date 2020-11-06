/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.translation;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.ModelInfoLoader;
import org.cqframework.cql.cql2elm.ModelInfoProvider;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.elm_modelinfo.r1.ModelInfo;

import com.ibm.cohort.engine.LibraryFormat;

/**
 * Common functionality for use when implementing CQL translator
 * wrapper implementations.
 */
public abstract class BaseCqlTranslationProvider implements CqlTranslationProvider {

	public static final LibraryFormat DEFAULT_TARGET_FORMAT = LibraryFormat.XML;

	public static void registerModelInfo(File modelInfoFile) {
		ModelInfo modelInfo = JAXB.unmarshal(modelInfoFile, ModelInfo.class);
		// Force mapping  to FHIR 4.0.1. Consider supporting different versions in the future.
		// Possibly add support for auto-loading model info files.
		modelInfo.setTargetVersion("4.0.1");
		modelInfo.setTargetUrl("http://hl7.org/fhir");
		org.hl7.elm.r1.VersionedIdentifier modelId = (new org.hl7.elm.r1.VersionedIdentifier()).withId(modelInfo.getName()).withVersion(modelInfo.getVersion());
		ModelInfoProvider modelProvider = () -> modelInfo;
		ModelInfoLoader.registerModelInfoProvider(modelId, modelProvider);
	}
	
	public List<Options> getDefaultOptions() {
		List<CqlTranslator.Options> defaults = new ArrayList<>(CqlTranslatorOptions.defaultOptions().getOptions());
		//defaults.add( CqlTranslator.Options.EnableDateRangeOptimization );
		return defaults;
	}
	
	@Override
	public Library translate(InputStream cql) throws Exception {
		return translate( cql, getDefaultOptions() );
	}
	
	@Override
	public Library translate(InputStream cql, List<Options> options) throws Exception {
		return translate(cql, options, DEFAULT_TARGET_FORMAT);
	}
}
