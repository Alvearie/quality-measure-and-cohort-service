/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.Base64;

import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.opencds.cqf.common.evaluation.LibraryLoader;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.common.providers.LibrarySourceProvider;

/**
 * Helper functions for working with FHIR Libraries
 */
public class LibraryHelper {

	/**
	 * Create a LibraryLoader using the provided LibraryResolutionProvider, but
	 * overriding the default behavior such that CQL text is base64 decoded.
	 * 
	 * @param provider Library resolution provider
	 * @return LibraryLoader that will base64 decode CQL text
	 */
	public static LibraryLoader createLibraryLoader(LibraryResolutionProvider<org.hl7.fhir.r4.model.Library> provider) {
		ModelManager modelManager = new ModelManager();
		LibraryManager libraryManager = new LibraryManager(modelManager);
		libraryManager.getLibrarySourceLoader().clearProviders();

		libraryManager.getLibrarySourceLoader().registerProvider(
				new LibrarySourceProvider<org.hl7.fhir.r4.model.Library, org.hl7.fhir.r4.model.Attachment>(provider,
						x -> x.getContent(), x -> x.getContentType(), x -> Base64.getDecoder().decode(x.getData())));

		return new LibraryLoader(libraryManager, modelManager);
	}
}
