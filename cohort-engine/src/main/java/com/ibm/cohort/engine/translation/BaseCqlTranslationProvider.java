/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.translation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.elm.execution.Library;

import com.ibm.cohort.engine.LibraryFormat;

/**
 * Common functionality for use when implementing CQL translator
 * wrapper implementations.
 */
public abstract class BaseCqlTranslationProvider implements CqlTranslationProvider {

	public static final LibraryFormat DEFAULT_TARGET_FORMAT = LibraryFormat.XML;
	
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

	@Override
	public Library translate(String cql) throws Exception {
		return translate( cql, getDefaultOptions() );
	}

	public Library translate(String cql, List<Options> options) throws Exception {
		return translate(cql, options, DEFAULT_TARGET_FORMAT);
	}
}
