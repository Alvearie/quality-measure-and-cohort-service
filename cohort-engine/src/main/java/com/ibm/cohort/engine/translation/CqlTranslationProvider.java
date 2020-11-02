/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.translation;

import java.io.InputStream;
import java.util.List;

import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.elm.execution.Library;

import com.ibm.cohort.engine.LibraryFormat;

/**
 * A general interface that can be used to abstract interaction with multiple
 * implementations of a CqlTranslator such as linking directly to the translator
 * JAR or calling out to the CqlTranslationService microservice.
 */
public interface CqlTranslationProvider {
	public Library translate(InputStream cql) throws Exception;

	public Library translate(InputStream cql, List<Options> options) throws Exception;

	public Library translate(InputStream cql, List<Options> options, LibraryFormat targetFormat) throws Exception;
}
