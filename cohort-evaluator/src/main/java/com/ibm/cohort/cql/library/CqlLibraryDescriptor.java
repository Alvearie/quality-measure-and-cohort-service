/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CqlLibraryDescriptor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String libraryId;
    private String version;
    private Format format;

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

    @JsonIgnore
    public CqlVersionedIdentifier getVersionedIdentifier() {
        return new CqlVersionedIdentifier(libraryId, version);
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
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append( "libraryId", libraryId )
                .append( "version", version )
                .append( "format", format )
                .build();
    }
}
