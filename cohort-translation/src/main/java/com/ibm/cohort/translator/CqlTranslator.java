/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.translator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.cqframework.cql.cql2elm.FhirLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelInfoLoader;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;

import com.ibm.cohort.translator.provider.CustomModelInfoProvider;

public class CqlTranslator {
	private ModelManager modelManager;
	private LibraryManager libraryManager;

	public CqlTranslator(LibrarySourceProvider provider){
		modelManager = new ModelManager();
		libraryManager = new LibraryManager(modelManager);
		libraryManager.getLibrarySourceLoader().registerProvider(provider);
		libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
	}

	public void addCustomModelInfo(File modelInfo){
		ModelInfoLoader.registerModelInfoProvider((new VersionedIdentifier()).withId("Custom").withVersion("1"), new CustomModelInfoProvider(modelInfo.getPath()));
	}

	public Library translateString(String cql, List<org.cqframework.cql.cql2elm.CqlTranslator.Options> options) throws IOException, JAXBException {

		org.cqframework.cql.cql2elm.CqlTranslator translator = org.cqframework.cql.cql2elm.CqlTranslator
				.fromText(cql,modelManager,libraryManager, options.toArray(new org.cqframework.cql.cql2elm.CqlTranslator.Options[0]));

		//todo do we want the ability to do anything else with this? the existing translator has the addition

		return CqlLibraryReader.read(new StringReader(translator.toXml()));
	}

	public Library translateStream(InputStream cql, List<org.cqframework.cql.cql2elm.CqlTranslator.Options> options) throws IOException, JAXBException {
		org.cqframework.cql.cql2elm.CqlTranslator translator = org.cqframework.cql.cql2elm.CqlTranslator
				.fromStream(cql,modelManager, libraryManager, options.toArray(new org.cqframework.cql.cql2elm.CqlTranslator.Options[0]));
		return CqlLibraryReader.read(new StringReader(translator.toXml()));
	}
}