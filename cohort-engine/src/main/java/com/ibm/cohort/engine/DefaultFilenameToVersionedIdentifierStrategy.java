/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import org.apache.commons.io.FilenameUtils;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * Extract library ID and version from a file name string. The default
 * assumption is that the filename will be in the format
 * "libraryName-libraryVersion.extension" where the libraryVersion is optional.
 */
public class DefaultFilenameToVersionedIdentifierStrategy implements FilenameToVersionedIdentifierStrategy {
	@Override
	public VersionedIdentifier filenameToVersionedIdentifier(String filename) {
		VersionedIdentifier id = null;
		
		String libraryId = FilenameUtils.getBaseName(filename);

		String version = null;

		int versionLoc = libraryId.lastIndexOf('-');
		// If the last dash is the last character of the name, it is part of the ID
		if (versionLoc != -1 && versionLoc < (libraryId.length() - 1)) {
			version = libraryId.substring(versionLoc + 1);
			libraryId = libraryId.substring(0, versionLoc);
		}

		id = new VersionedIdentifier().withId(libraryId).withVersion(version);

		return id;
	}
}
