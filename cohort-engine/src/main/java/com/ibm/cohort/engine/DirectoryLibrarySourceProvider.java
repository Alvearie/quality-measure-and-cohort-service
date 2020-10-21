/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * Directory-based implementation of MultiFormatLibrarySourceProvider. Filenames can contain
 * library ID and version based on the logic in the provided FilenameToVersionedIdentifierStrategy.
 * When no strategy is provided {@link DefaultFilenameToVersionedIdentifierStrategy} is used.
 */
public class DirectoryLibrarySourceProvider extends MultiFormatLibrarySourceProvider {
	
	public DirectoryLibrarySourceProvider(Path folderPath) throws Exception {
		this( folderPath, new DefaultFilenameToVersionedIdentifierStrategy() );
	}
	
	public DirectoryLibrarySourceProvider(Path folderPath, FilenameToVersionedIdentifierStrategy idStrategy) throws Exception {
		if( ! folderPath.toFile().isDirectory() ) {
			throw new IllegalArgumentException("Path is not a directory");
		}
		
		final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path p) {
				return LibraryFormat.isSupportedPath( p );
			}
		};

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, filter)) {
			for (Path entry : stream) {
				try (InputStream is = Files.newInputStream(entry)) {
					String filename = entry.getFileName().toString();
					
					VersionedIdentifier id = idStrategy.filenameToVersionedIdentifier(filename);
					
					LibraryFormat sourceFormat = LibraryFormat.forPath( entry );
					Map<LibraryFormat, InputStream> formats = sources.get( id );
					if( formats == null ) {
						formats = new HashMap<>();
						sources.put( id, formats);
					}
					
					String text = FileUtils.readFileToString(entry.toFile(), StandardCharsets.UTF_8);
					formats.put(sourceFormat, new ByteArrayInputStream( text.getBytes() ) );
				}
			}
		}
	}
}
