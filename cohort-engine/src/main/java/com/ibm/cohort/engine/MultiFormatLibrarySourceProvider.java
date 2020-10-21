/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * The MultiFormatLibrarySourceProvider provides collection capabilities 
 * for dealing with source collections that contain CQL libraries in 
 * multiple formats (e.g. CQL + ELM-XML + ELM+JSON) and implements 
 * the CQL Translator LibrarySourceProvider interface so that users
 * that need or want CQL translation can easily get it from the provided
 * libraries.
 */
public class MultiFormatLibrarySourceProvider implements LibrarySourceProvider {

	protected Map<VersionedIdentifier, Map<LibraryFormat, InputStream>> sources = new HashMap<>();
	
	/**
	 * Retrieve the stream pointer for the Library of the given identifier and source format.
	 * @param libraryIdentifier Library identifier and optional version.
	 * @param sourceFormat Source format of the stream of interest
	 * @return InputStream where source data can be read.
	 */
	public InputStream getLibrarySource( VersionedIdentifier libraryIdentifier, LibraryFormat sourceFormat ) {
		InputStream result = null;
		
		Map<LibraryFormat,InputStream> byLibrary = sources.get( libraryIdentifier );
		if( byLibrary != null ) {
			result = byLibrary.get( sourceFormat );
		}
		
		return result;
	}
	
	/**
	 * Retrieve all sources in the collection of a given source format.
	 * @param sourceFormat source format of interest
	 * @return Map of library identifier to InputStream containing the source data.
	 */
	public Map<VersionedIdentifier, InputStream> getSourcesByFormat(LibraryFormat sourceFormat) {
		Map<VersionedIdentifier, InputStream> filtered = new HashMap<>();
		for (Map.Entry<VersionedIdentifier, Map<LibraryFormat,InputStream>> entry : sources.entrySet()) {
			InputStream is = entry.getValue().get( sourceFormat );
			if( is != null ) {
				filtered.put( entry.getKey(), is );
			}
		}
		return filtered;
	}

	@Override
	public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
		return getSourcesByFormat( LibraryFormat.CQL ).get(libraryIdentifier);
	}
}
