/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

public class DirectoryBasedCqlLibraryProvider implements CqlLibraryProvider {
    
    private File directory;
    
    public DirectoryBasedCqlLibraryProvider(File directory) {
        this.directory = directory;
    }
    
    @Override
    public Collection<CqlLibraryDescriptor> listLibraries() {
        return Arrays.stream( directory.listFiles() )
                .map( f -> f.getName() )
                .map( CqlLibraryHelpers::filenameToLibraryDescriptor )
                .filter( Objects::nonNull )
                .collect(Collectors.toSet());
    }

    @Override
    public CqlLibrary getLibrary(CqlLibraryDescriptor libraryDescriptor) {
        CqlLibrary library = null;
        
        File file = new File( directory, CqlLibraryHelpers.libraryDescriptorToFilename(libraryDescriptor) );
        if( file.exists() ) {
            try {
                library = new CqlLibrary()
                        .setDescriptor(libraryDescriptor)
                        .setContent(FileUtils.readFileToString(file, Charset.defaultCharset()));
            } catch( IOException iex ) {
                throw new RuntimeException("Failed to deserialize library", iex);
            }
        }
        
        return library;
    }
}
