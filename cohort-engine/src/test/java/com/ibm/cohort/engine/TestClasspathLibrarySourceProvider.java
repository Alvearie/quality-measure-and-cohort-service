/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.elm.r1.VersionedIdentifier;

public class TestClasspathLibrarySourceProvider extends MultiFormatLibrarySourceProvider {
	public TestClasspathLibrarySourceProvider(List<String> libraryResources,
			FilenameToVersionedIdentifierStrategy idStrategy) {
		for (String resource : libraryResources) {
			VersionedIdentifier vid = idStrategy.filenameToVersionedIdentifier(resource);
			Map<LibraryFormat, String> formats = sources.computeIfAbsent(vid, key -> new HashMap<>());
			InputStream is = ClassLoader.getSystemResourceAsStream(resource);
			if( is == null ) {
				throw new IllegalArgumentException( resource );
			}
			String resourceAsString = new BufferedReader(
					new InputStreamReader(is, StandardCharsets.UTF_8))
					.lines()
					.collect(Collectors.joining("\n"));
			formats.put(LibraryFormat.forString(resource), resourceAsString);
		}
	}
}
