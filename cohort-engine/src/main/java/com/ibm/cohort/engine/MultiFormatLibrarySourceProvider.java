/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

	Map<VersionedIdentifier, Map<LibraryFormat, String>> sources = new HashMap<>();

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
	public String getLibrarySource(VersionedIdentifier libraryIdentifier, LibraryFormat sourceFormat) {
		String result = null;

		Map<LibraryFormat, String> byLibrary = sources.get(libraryIdentifier);
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
	public Map<VersionedIdentifier, String> getSourcesByFormat(LibraryFormat sourceFormat) {
		Map<VersionedIdentifier, String> filtered = new HashMap<>();
		for (Map.Entry<VersionedIdentifier, Map<LibraryFormat, String>> entry : sources.entrySet()) {
			Map<LibraryFormat, String> x = entry.getValue();
			if(x != null && x.get(sourceFormat) != null) {
				filtered.put(entry.getKey(), x.get(sourceFormat));
			}
		}
		return filtered;
	}

	@Override
	public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
		String library = getSourcesByFormat(LibraryFormat.CQL).get(libraryIdentifier);
		return (library == null) ? null : new ByteArrayInputStream(library.getBytes());
	}

	private static void addClasspathFhirHelpers(Map<VersionedIdentifier, Map<LibraryFormat, String>> sources, VersionedIdentifier libraryIdentifier){
		Map<LibraryFormat, String> specFormat = sources.computeIfAbsent(libraryIdentifier, key -> new HashMap<>());
		if(specFormat.isEmpty()) {
			InputStream fhirHelperResource = MultiFormatLibrarySourceProvider.class.getResourceAsStream(
					String.format("/org/hl7/fhir/%s-%s.xml",
							libraryIdentifier.getId(),
							libraryIdentifier.getVersion()));
			String fhirHelperResourceAsString = new BufferedReader(
					new InputStreamReader(fhirHelperResource, StandardCharsets.UTF_8))
					.lines()
					.collect(Collectors.joining("\n"));
			specFormat.put(LibraryFormat.XML, fhirHelperResourceAsString);
			InputStream fhirHelperResourceCQL = MultiFormatLibrarySourceProvider.class.getResourceAsStream(
					String.format("/org/hl7/fhir/%s-%s.cql",
							libraryIdentifier.getId(),
							libraryIdentifier.getVersion()));
			String fhirHelperResourceCQLAsString = new BufferedReader(
					new InputStreamReader(fhirHelperResourceCQL, StandardCharsets.UTF_8))
					.lines()
					.collect(Collectors.joining("\n"));
			specFormat.put(LibraryFormat.CQL, fhirHelperResourceCQLAsString);
		}
	}
}
