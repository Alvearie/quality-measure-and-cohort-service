/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.translation;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

import com.ibm.cohort.engine.LibraryFormat;

/**
 * Uses the CqlTranslator inprocess to convert CQL to ELM. 
 */
public class InJVMCqlTranslationProvider extends BaseCqlTranslationProvider {

	private static final Logger LOG = LoggerFactory.getLogger(InJVMCqlTranslationProvider.class);
	private ModelManager modelManager;
	private LibraryManager libraryManager;

	public InJVMCqlTranslationProvider() {
		this.modelManager = new ModelManager();
		this.libraryManager = new LibraryManager(modelManager);
		libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
	}

	public InJVMCqlTranslationProvider(LibraryManager libraryManager, ModelManager modelManager) {
		this.modelManager = modelManager;
		this.libraryManager = libraryManager;
	}
	
	public InJVMCqlTranslationProvider(LibrarySourceProvider provider) {
		this();
		addLibrarySourceProvider(provider);
	}

	public InJVMCqlTranslationProvider addLibrarySourceProvider(LibrarySourceProvider provider) {
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

		CqlTranslator translator = CqlTranslator.fromStream(cql, modelManager, libraryManager, ucumService,
				CqlTranslatorException.ErrorSeverity.Info, signatureLevel,
				optionsList.toArray(new Options[optionsList.size()]));

		LOG.debug("Translated CQL contains {} errors", translator.getErrors().size());
		if (translator.getErrors().size() > 0) {
			throw new Exception("CQL translation contained errors: " + String.join("\n",
					translator.getErrors().stream().map(x -> x.toString()).collect(Collectors.toList())));
		}

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
