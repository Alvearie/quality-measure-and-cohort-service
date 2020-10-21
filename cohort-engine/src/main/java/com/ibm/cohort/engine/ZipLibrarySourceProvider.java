/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * Provide an implementation of the LibrarySourceProvider that
 * can operate on a ZipFile as input. The versioned identifier
 * to filename handling is modeled after the DefaultLibrarySourceProvider
 * implementation provided by the CQL Engine.
 */
public class ZipLibrarySourceProvider implements LibrarySourceProvider {

	ZipFile zipFile = null;

	public ZipLibrarySourceProvider(ZipFile zipFile) {
		this.zipFile = zipFile;
	}

	@Override
	public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
		InputStream result = null;

		// Mimic what DefaultLibrarySourceProvider does for filesystem paths
		String libraryName = libraryIdentifier.getId();
		String fileName = String.format("%s%s.cql", libraryName,
				libraryIdentifier.getVersion() != null ? ("-" + libraryIdentifier.getVersion()) : "");

		ZipEntry entry = zipFile.getEntry(fileName);
		if (entry != null) {
			try {
				result = zipFile.getInputStream(entry);
			} catch (IOException iex) {
				throw new RuntimeException("Failed to load entry from ZIP", iex);
			}
		}
		return result;
	}

}
