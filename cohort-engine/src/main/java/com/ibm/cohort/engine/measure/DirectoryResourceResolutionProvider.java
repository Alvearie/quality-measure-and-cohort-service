/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import ca.uhn.fhir.parser.IParser;

/**
 * Implementation of a FHIR knowledge artifact resource resolution provider that is based
 * on data in a filesystem folder.
 */
public class DirectoryResourceResolutionProvider extends ResourceResolutionProvider {
	public DirectoryResourceResolutionProvider(File directory, IParser parser, String... searchPaths) throws IOException {
		this( directory.toPath(), parser, searchPaths );
	}		
	
	public DirectoryResourceResolutionProvider(Path path, IParser parser, String... searchPaths) throws IOException {
	
		final String [] prefixedSearchPaths = (searchPaths != null) ? new String[ searchPaths.length ] : null;
		if( searchPaths != null ) {
			for( int i=0; i<searchPaths.length; i++ ) {
				prefixedSearchPaths[i] = Paths.get(path.toString(), searchPaths[i]).toString();
			}
		}
		
		Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path entry, BasicFileAttributes attrs) throws IOException {
					
				if( isFileAllowed(entry.toString(), prefixedSearchPaths) ) {
					try( InputStream is = Files.newInputStream(entry) ) {
						processResource(entry.toString(), is, parser);
					}
				}
				
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
