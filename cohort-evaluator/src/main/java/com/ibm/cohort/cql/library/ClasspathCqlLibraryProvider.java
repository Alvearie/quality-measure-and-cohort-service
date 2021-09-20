/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class ClasspathCqlLibraryProvider implements CqlLibraryProvider {

    private List<String> packages;
    
    public ClasspathCqlLibraryProvider(String packageName, String... packageNames ) {
        packages = new ArrayList<>();
        packages.add( packageName );
        packages.addAll( Arrays.asList(packageNames) );
    }
    
    @Override
    public Collection<CqlLibraryDescriptor> listLibraries() {
        // ClassLoaders don't inherently want to list the classes they expose. It is 
        // possible through something like guava 14+, but I'm not sure it is worth it 
        // here to take that dependency. Library listing isn't a strict requirement
        return Collections.emptyList();
    }

    @Override
    public CqlLibrary getLibrary(CqlLibraryDescriptor cqlResourceDescriptor) {
        CqlLibrary library = null;
        
        String filename = CqlLibraryHelpers.libraryDescriptorToFilename(cqlResourceDescriptor);
        for( String packageName : packages ) {
            
            String name = String.format("%s/%s", packageName.replace('.', '/'), filename);
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name) ) {
                if( is != null ) {
                    CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                            .setLibraryId(cqlResourceDescriptor.getLibraryId())
                            .setVersion(cqlResourceDescriptor.getVersion())
                            .setFormat(cqlResourceDescriptor.getFormat())
                            .setExternalId(name);
                    
                    library = new CqlLibrary();
                    library.setDescriptor(descriptor);
                    library.setContent( IOUtils.toString(is, "utf-8") );
                    break;
                }
            } catch( IOException iex ) {
                throw new CqlLibraryDeserializationException(iex);
            }
        }
        
        return library;
    }

}
