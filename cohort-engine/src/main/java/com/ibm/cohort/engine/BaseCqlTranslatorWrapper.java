/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.elm.execution.Library;

/**
 * Common functionality for use when implementing CQL translator
 * wrapper implementations.
 */
public abstract class BaseCqlTranslatorWrapper implements CqlTranslatorWrapper {

	public static final LibraryFormat DEFAULT_TARGET_FORMAT = LibraryFormat.XML;
	
	public List<Options> getDefaultOptions() {
		return new ArrayList<>(CqlTranslatorOptions.defaultOptions().getOptions());
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
