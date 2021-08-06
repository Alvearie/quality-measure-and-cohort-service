/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library.s3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.s3.AmazonS3;
import com.ibm.cohort.cql.aws.AWSClientHelpers;
import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryHelpers;
import com.ibm.cohort.cql.library.CqlLibraryProvider;

public class S3CqlLibraryProvider implements CqlLibraryProvider {

    private AmazonS3 client;
    private String bucket;
    private String key;

    public S3CqlLibraryProvider(AmazonS3 client, String bucket, String key) {
        this.client = client;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public Collection<CqlLibraryDescriptor> listLibraries() {
        Set<CqlLibraryDescriptor> libraries = new HashSet<>();
        AWSClientHelpers.processS3ObjectKeys(client, bucket, key, osm -> {
            libraries.add( CqlLibraryHelpers.filenameToLibraryDescriptor(osm.getKey()) );
        });
        return libraries;
    }

    @Override
    public CqlLibrary getLibrary(CqlLibraryDescriptor libraryDescriptor) {
        CqlLibrary library = null;
        
        String objectName = key + "/" + CqlLibraryHelpers.libraryDescriptorToFilename(libraryDescriptor);
        if( client.doesObjectExist(bucket, objectName) ) {
            String contents = client.getObjectAsString(bucket, objectName);
            library = new CqlLibrary()
                    .setDescriptor(libraryDescriptor)
                    .setContent(contents);
        }
        
        return library;
    }
}
