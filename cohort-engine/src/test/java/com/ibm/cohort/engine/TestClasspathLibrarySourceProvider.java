/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.elm.r1.VersionedIdentifier;

public class TestClasspathLibrarySourceProvider extends MultiFormatLibrarySourceProvider {
	public TestClasspathLibrarySourceProvider(List<String> libraryResources,
			FilenameToVersionedIdentifierStrategy idStrategy) {
		for (String resource : libraryResources) {
			VersionedIdentifier vid = idStrategy.filenameToVersionedIdentifier(resource);
			Map<LibraryFormat, InputStream> formats = sources.computeIfAbsent(vid, key -> new HashMap<>());
			InputStream is = ClassLoader.getSystemResourceAsStream(resource);
			if( is == null ) {
				throw new IllegalArgumentException( resource );
			}
			formats.put(LibraryFormat.forString(resource), is);
		}
		addClasspathFhirHelpers(sources);
	}
}
