/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

/**
 * Implementation of the CQL Engine LibraryLoader interface that
 * supports on-demand translation as necessary.
 */
public class TranslatingLibraryLoader implements LibraryLoader {

	private MultiFormatLibrarySourceProvider provider;
	private Map<VersionedIdentifier, Library> libraryCache = new HashMap<>();
	private CqlTranslationProvider translator;
	private boolean isForceTranslation = false;

	public void setSourceProvider(MultiFormatLibrarySourceProvider provider) {
		this.provider = provider;
	}

	public void setTranslationProvider(CqlTranslationProvider translator) {
		this.translator = translator;
	}
	
	public void setForceTranslation( boolean forceTranslation ) {
		this.isForceTranslation = forceTranslation;
	}

	@Override
	public Library load(VersionedIdentifier libraryIdentifier) {
		Library library = libraryCache.get(libraryIdentifier);
		if (library == null) {
			org.hl7.elm.r1.VersionedIdentifier translatorVersionedId = new org.hl7.elm.r1.VersionedIdentifier()
					.withId(libraryIdentifier.getId()).withVersion(libraryIdentifier.getVersion());

			try {
				if( ! isForceTranslation ) {
					InputStream is = provider.getLibrarySource(translatorVersionedId, LibraryFormat.XML);
					if (is != null) {
						library = CqlLibraryReader.read(is);
					} 
				} 
				
				if( library == null ) {
					InputStream is = provider.getLibrarySource(translatorVersionedId, LibraryFormat.CQL);
					library = translator.translate(is);
				}
				
				libraryCache.put(libraryIdentifier, library);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to load library", ex);
			}
		}

		return library;
	}
}
