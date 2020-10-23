/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.FhirLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.fhir.ucum.UcumService;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the CqlTranslator inprocess to convert CQL to ELM. This is
 * an alternative to using the CQL Translation Service microservice
 * which is a longer term goal and something we've tested with, but
 * are not ready to deliver.
 */
public class InJVMCqlTranslatorWrapper extends BaseCqlTranslatorWrapper {

	private static final Logger LOG = LoggerFactory.getLogger(InJVMCqlTranslatorWrapper.class);
	private ModelManager modelManager;
	private LibraryManager libraryManager;

	public InJVMCqlTranslatorWrapper() {
		this.modelManager = new ModelManager();
		this.libraryManager = new LibraryManager(modelManager);
	}

	public InJVMCqlTranslatorWrapper addLibrarySourceProvider(LibrarySourceProvider provider) {
		libraryManager.getLibrarySourceLoader().registerProvider(provider);
		return this;
	}

	@Override
	public Library translate(InputStream cql, List<Options> options, LibraryFormat targetFormat) throws Exception {
		Library result = null;

		UcumService ucumService = null;
		LibraryBuilder.SignatureLevel signatureLevel = LibraryBuilder.SignatureLevel.None;

		List<Options> optionsList = new ArrayList<Options>();
		if (options != null) {
			optionsList.addAll(options);
		}

		libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
		CqlTranslator translator = CqlTranslator.fromStream(cql, modelManager, libraryManager, ucumService,
				CqlTranslatorException.ErrorSeverity.Info, signatureLevel,
				optionsList.toArray(new Options[optionsList.size()]));

		LOG.debug("Translated CQL contains {} errors", translator.getErrors().size());

		switch (targetFormat) {
		case XML:
			result = CqlLibraryReader.read(new StringReader(translator.toXml()));
			break;
// This is only a theoretical nice-to-have and fails deserialization, so disabling support for now.
//		case JSON:
//			result = JsonCqlLibraryReader.read(new StringReader(translator.toJxson()));
//			break;
		default:
			throw new IllegalArgumentException(
					String.format("The CQL Engine does not support format %s", targetFormat.name()));
		}

		return result;
	}

}
