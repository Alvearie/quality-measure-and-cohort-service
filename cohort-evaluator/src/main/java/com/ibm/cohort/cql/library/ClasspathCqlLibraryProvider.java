/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class ClasspathCqlLibraryProvider implements CqlLibraryProvider {

    public static final String FHIR_HELPERS_CLASSPATH = "org.hl7.fhir";

    private List<String> packages;
    private Set<Format> supportedFormats = null;

    public ClasspathCqlLibraryProvider() {
        this(FHIR_HELPERS_CLASSPATH);
    }

    public ClasspathCqlLibraryProvider(String packageName, String... packageNames ) {
        packages = new ArrayList<>();
        packages.add( packageName );
        packages.addAll( Arrays.asList(packageNames) );
    }
    
    public void setSupportedFormats(Format... formats) {
        this.supportedFormats = new HashSet<>(Arrays.asList(formats));
    }
    
    public Set<Format> getSupportedFormats() {
        return this.supportedFormats;
    }

    @Override
    public CqlLibrary getLibrary(CqlLibraryDescriptor cqlResourceDescriptor) {
        CqlLibrary library = null;
        
        if( supportedFormats == null || supportedFormats.contains(cqlResourceDescriptor.getFormat()) ) {
            String filename = CqlLibraryHelpers.libraryDescriptorToFilename(cqlResourceDescriptor);
            for( String packageName : packages ) {
                
                String name = String.format("%s/%s", packageName.replace('.', '/'), filename);
                try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name) ) {
                    if( is != null ) {
                        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                                .setLibraryId(cqlResourceDescriptor.getLibraryId())
                                .setVersion(cqlResourceDescriptor.getVersion())
                                .setFormat(cqlResourceDescriptor.getFormat());
                        
                        library = new CqlLibrary();
                        library.setDescriptor(descriptor);
                        library.setContent( IOUtils.toString(is, "utf-8") );
                        break;
                    }
                } catch( IOException iex ) {
                    throw new CqlLibraryDeserializationException(iex);
                }
            }
        }
        
        return library;
    }

}
