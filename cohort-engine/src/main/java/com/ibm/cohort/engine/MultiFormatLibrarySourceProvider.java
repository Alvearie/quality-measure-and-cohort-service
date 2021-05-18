/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * The MultiFormatLibrarySourceProvider provides collection capabilities for
 * dealing with source collections that contain CQL libraries in multiple
 * formats (e.g. CQL + ELM+XML + ELM+JSON) and implements the CQL Translator
 * LibrarySourceProvider interface so that users that need or want CQL
 * translation can easily get it from the provided libraries.
 */
public class MultiFormatLibrarySourceProvider implements LibrarySourceProvider {

	protected Map<VersionedIdentifier, Map<LibraryFormat, InputStream>> sources = new HashMap<>();

	/**
	 * Retrieve the stream for the Library content that matches the given identifier and
	 * source format. If a specific version is requested and that version is not found, an
	 * attempt is made to resolve the name with a null version. If no specific version is
	 * requested, an attempt is made to resolve a version with the same ID and the
	 * version that sorts last using a default string sort (will resolve to the newest
	 * semantic version, but is unclear when semantic versions are not used).
	 * 
	 * @param libraryIdentifier Library identifier and optional version.
	 * @param sourceFormat      Source format of the stream of interest
	 * @return InputStream where source data can be read.
	 */
	public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier, LibraryFormat sourceFormat) {
		InputStream result = null;

		Map<LibraryFormat, InputStream> byLibrary = sources.get(libraryIdentifier);
		if (byLibrary == null) {
			if (libraryIdentifier.getVersion() != null) {
				// Check for a library with the same name, but no version
				byLibrary = sources.get(new VersionedIdentifier().withId(libraryIdentifier.getId()));
			} else {
				// Check for the "newest" (last in string sort order) version
				Optional<VersionedIdentifier> newestVersion = sources.keySet().stream()
						.filter(vid -> vid.getId().equals(libraryIdentifier.getId())).sorted().reduce((x, y) -> y);
				if (newestVersion.isPresent()) {
					byLibrary = sources.get(newestVersion.get());
				}
			}
		}

		if(byLibrary == null && libraryIdentifier.getId().equals("FHIRHelpers")){
			addClasspathFhirHelpers(sources, libraryIdentifier);
			byLibrary = sources.get(libraryIdentifier);
		}

		if (byLibrary != null) {
			result = byLibrary.get(sourceFormat);
		}

		return result;
	}

	/**
	 * Retrieve all sources in the collection of a given source format.
	 * 
	 * @param sourceFormat source format of interest
	 * @return Map of library identifier to InputStream containing the source data.
	 */
	public Map<VersionedIdentifier, InputStream> getSourcesByFormat(LibraryFormat sourceFormat) {
		Map<VersionedIdentifier, InputStream> filtered = new HashMap<>();
		for (Map.Entry<VersionedIdentifier, Map<LibraryFormat, InputStream>> entry : sources.entrySet()) {
			InputStream is = entry.getValue().get(sourceFormat);
			if (is != null) {
				filtered.put(entry.getKey(), is);
			}
		}
		return filtered;
	}

	@Override
	public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
		return getSourcesByFormat(LibraryFormat.CQL).get(libraryIdentifier);
	}

	//todo deduplicate
	public static void addClasspathFhirHelpers(Map<VersionedIdentifier, Map<LibraryFormat, InputStream>> sources, VersionedIdentifier libraryIdentifier){
		Map<LibraryFormat, InputStream> specFormat = sources.computeIfAbsent(libraryIdentifier, key -> new HashMap<>());
		if(specFormat.isEmpty()) {
			InputStream fhirHelperResource = ClasspathLibrarySourceProvider.class.getResourceAsStream(
					String.format("/org/hl7/fhir/%s-%s.xml",
							libraryIdentifier.getId(),
							libraryIdentifier.getVersion()));
			specFormat.put(LibraryFormat.XML, fhirHelperResource);
			InputStream fhirHelperResourceCQL = ClasspathLibrarySourceProvider.class.getResourceAsStream(
					String.format("/org/hl7/fhir/%s-%s.cql",
							libraryIdentifier.getId(),
							libraryIdentifier.getVersion()));
			specFormat.put(LibraryFormat.CQL, fhirHelperResourceCQL);
		}
	}
}
