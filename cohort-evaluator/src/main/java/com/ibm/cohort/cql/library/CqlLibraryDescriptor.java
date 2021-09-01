/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CqlLibraryDescriptor implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum Format {
        CQL("cql", "text/cql", "application/cql"),
        ELM("XML", "xml", "application/elm+xml");
        
        private List<String> names;
        private Format(String... aliases) {
            this.names = new ArrayList<>();
            this.names.add(this.name());
            this.names.addAll(Arrays.asList(aliases));
        }
        
        public List<String> getNames() {
            return this.names;
        }
        
        private static Map<String,Format> INDEX = new HashMap<>(Format.values().length);
        static {
            for( Format fmt : Format.values() ) {
                for( String name : fmt.getNames() ) {
                    INDEX.put(name, fmt);
                }
            }
        }
        
        /**
         * Allow client code to do name to enum conversion using the 
         * enum's formal name or any of its aliases. 
         * 
         * @param name name of the enum to lookup
         * @return value of the resolved enum or null if no enum is found matching the name
         */
        public static Format lookupByName(String name) {
            return INDEX.get(name);
        }
    };
    
    private String libraryId;
    private String version;
    private Format format;
    private String externalId;

    public String getLibraryId() {
        return libraryId;
    }
    
    public CqlLibraryDescriptor setLibraryId(String libraryId) {
        this.libraryId = libraryId;
        return this;
    }
    
    public String getVersion() {
        return version;
    }

    public CqlLibraryDescriptor setVersion(String version) {
        this.version = version;
        return this;
    }

    public Format getFormat() {
        return format;
    }

    public CqlLibraryDescriptor setFormat(Format format) {
        this.format = format;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public CqlLibraryDescriptor setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append( libraryId )
                .append( version )
                .append( format )
                .build();
    }
    
    @Override
    public boolean equals(Object o2) {
        boolean isEqual = false;
        if( o2 != null && o2 instanceof CqlLibraryDescriptor ) {
            CqlLibraryDescriptor rhs = (CqlLibraryDescriptor) o2;
            isEqual = new EqualsBuilder()
                    .append(this.getLibraryId(), rhs.getLibraryId())
                    .append(this.getVersion(),  rhs.getVersion())
                    .append(this.getFormat(), rhs.getFormat())
                    .build();
        } 
        return isEqual;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append( libraryId )
                .append( version )
                .append( format )
                .append( externalId )
                .build();
    }
}
