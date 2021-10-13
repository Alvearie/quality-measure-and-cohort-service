/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import org.apache.commons.io.FilenameUtils;

import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;

public class CqlLibraryHelpers {
    public static CqlLibraryDescriptor filenameToLibraryDescriptor(String filename) {
        CqlLibraryDescriptor id = null;
        
        String libraryId = FilenameUtils.getBaseName(filename);
        Format format = Format.lookupByName( FilenameUtils.getExtension(filename).toUpperCase() );
        if( format != null ) {
            String version = null;
    
            int versionLoc = libraryId.lastIndexOf('-');
            // If the last dash is the last character of the name, it is part of the ID
            if (versionLoc != -1 && versionLoc < (libraryId.length() - 1)) {
                version = libraryId.substring(versionLoc + 1);
                libraryId = libraryId.substring(0, versionLoc);
            }
    
            id = new CqlLibraryDescriptor()
                    .setLibraryId(libraryId)
                    .setVersion(version)
                    .setFormat(format);
        }

        return id;
    }
    
    public static String libraryDescriptorToFilename(CqlLibraryDescriptor descriptor) { 
        return String.format("%s-%s.%s", descriptor.getLibraryId(), descriptor.getVersion(), ( descriptor.getFormat() == Format.CQL ) ? "cql" : "xml");
    }
}
