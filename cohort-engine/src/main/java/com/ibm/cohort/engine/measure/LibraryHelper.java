/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.common.providers.LibrarySourceProvider;

import com.ibm.cohort.engine.translation.InJVMCqlTranslationProvider;

/**
 * Helper functions for working with FHIR Libraries
 */
public class LibraryHelper {

	/**
	 * Create a LibraryLoader using the provided LibraryResolutionProvider.
	 * 
	 * @param provider Library resolution provider
	 * @return LibraryLoader that will base64 decode CQL text
	 */
	public static LibraryLoader createLibraryLoader(LibraryResolutionProvider<org.hl7.fhir.r4.model.Library> provider) {
		InJVMCqlTranslationProvider translator = new InJVMCqlTranslationProvider();
		translator.addLibrarySourceProvider(new LibrarySourceProvider<org.hl7.fhir.r4.model.Library, org.hl7.fhir.r4.model.Attachment>(provider,
				x -> x.getContent(), x -> x.getContentType(), x -> x.getData()));

		return new LibraryLoader(provider, translator);
	}
}
