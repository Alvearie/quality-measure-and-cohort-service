///*
// *
// *  * (C) Copyright IBM Corp. 2021
// *  *
// *  * SPDX-License-Identifier: Apache-2.0
// *
// */
//
//package com.ibm.cohort.translator.provider;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.cqframework.cql.cql2elm.CqlTranslator;
//import org.cqframework.cql.cql2elm.CqlTranslatorException;
//import org.cqframework.cql.cql2elm.LibraryBuilder;
//import org.cqframework.cql.cql2elm.LibraryManager;
//import org.cqframework.cql.cql2elm.ModelManager;
//import org.cqframework.cql.elm.execution.Library;
//import org.fhir.ucum.UcumService;
//
//public class JustTheStuff {
//	private ModelManager modelManager;
//	private LibraryManager libraryManager;
//
//	public Library translate(InputStream cql, List<CqlTranslator.Options> options, LibraryFormat targetFormat) throws IOException {
//		Library result;
//
//		UcumService ucumService = null;
//		LibraryBuilder.SignatureLevel signatureLevel = LibraryBuilder.SignatureLevel.None;
//
//		List<CqlTranslator.Options> optionsList = new ArrayList<>();
//		if (options != null) {
//			optionsList.addAll(options);
//		}
//
//		CqlTranslator translator = CqlTranslator.fromStream(cql, modelManager, libraryManager, ucumService,
//				CqlTranslatorException.ErrorSeverity.Info, signatureLevel,
//				optionsList.toArray(new CqlTranslator.Options[0]));
//	}
//}
