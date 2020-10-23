/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * ZIP archive-based implementation of MultiFormatLibrarySourceProvider. Filenames can contain
 * library ID and version based on the logic in the provided FilenameToVersionedIdentifierStrategy.
 * When no strategy is provided {@link DefaultFilenameToVersionedIdentifierStrategy} is used.
 */
public class ZipStreamLibrarySourceProvider extends MultiFormatLibrarySourceProvider {
	
	public ZipStreamLibrarySourceProvider(ZipInputStream zipInputStream) throws Exception {
		this( zipInputStream, new DefaultFilenameToVersionedIdentifierStrategy() );
	}
	
	public ZipStreamLibrarySourceProvider(ZipInputStream zipInputStream, FilenameToVersionedIdentifierStrategy idStrategy) throws Exception {
		
		ZipEntry ze;
		while ((ze = zipInputStream.getNextEntry()) != null) {
			VersionedIdentifier id = idStrategy.filenameToVersionedIdentifier(ze.getName());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(zipInputStream, baos);
			Map<LibraryFormat, InputStream> formats = sources.get(id);
			if (formats == null) {
				formats = new HashMap<>();
				sources.put(id, formats);
			}
			formats.put(LibraryFormat.forString(ze.getName()),
					new ByteArrayInputStream(baos.toByteArray()));
		}
	}


}
