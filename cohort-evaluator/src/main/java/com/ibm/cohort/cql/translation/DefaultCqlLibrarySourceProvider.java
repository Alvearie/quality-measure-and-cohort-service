/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import java.io.InputStream;

import org.hl7.elm.r1.VersionedIdentifier;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;

public class DefaultCqlLibrarySourceProvider implements CqlLibrarySourceProvider {
    private CqlLibraryProvider libraryProvider;

    public DefaultCqlLibrarySourceProvider(CqlLibraryProvider libraryProvider) {
        this.libraryProvider = libraryProvider;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor().setLibraryId(libraryIdentifier.getId())
                .setVersion(libraryIdentifier.getVersion()).setFormat(Format.CQL);
        
        CqlLibrary library = libraryProvider.getLibrary(descriptor);
        if( library != null ) {
            return library.getContentAsStream();
        } else {
            throw new IllegalArgumentException(String.format("Missing required library '%s' version '%s' format '%s' not found", libraryIdentifier.getId(), libraryIdentifier.getVersion(), Format.CQL.name()));
        }
    }
}
