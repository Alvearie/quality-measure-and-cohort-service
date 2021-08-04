/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Encapsulates the contents of a particular CQL library in 
 * source form (CQL), transpiled form (ELM), or both.
 */
public class CqlLibrary {
    private CqlLibraryDescriptor descriptor;
    private String content;
    
    public CqlLibraryDescriptor getDescriptor() {
        return descriptor;
    }
    public CqlLibrary setDescriptor(CqlLibraryDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }
    public String getContent() {
        return content;
    }
    public CqlLibrary setContent(String contents) {
        this.content = contents;
        return this;
    }
    public InputStream getContentAsStream() {
        return new ByteArrayInputStream(content.getBytes());
    }
    
    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }
    
    @Override
    public boolean equals(Object o2) {
        boolean isEqual = false;
        if( o2 != null && o2 instanceof CqlLibrary ) {
            isEqual = this.getDescriptor().equals(((CqlLibrary)o2).getDescriptor());
        }
        return isEqual;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(descriptor.toString())
                .append(content)
                .build();
    }
}
