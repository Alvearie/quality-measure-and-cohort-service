/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;
import javax.xml.namespace.QName;

import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.util.EqualsStringMatcher;
import com.ibm.cohort.cql.util.StringMatcher;

public class DataTypeRequirementsProcessor {
    
    public static class DataTypeRequirements {
        private Map<String,Set<String>> pathsByDataType;
        private Map<String,Set<StringMatcher>> pathMatchersByDataType;
        
        public DataTypeRequirements(Map<String,Set<String>> pathsByDataType, Map<String,Set<StringMatcher>> pathMatchersByDataType) {
            this.pathsByDataType = pathsByDataType;
            this.pathMatchersByDataType = pathMatchersByDataType;
        }

        public Map<String,Set<String>> getPathsByDataType() {
            return pathsByDataType;
        }
        
        public Map<String, Set<StringMatcher>> getPathMatchersByDataType() {
            return pathMatchersByDataType;
        }
        
        public Map<String,Set<StringMatcher>> allAsStringMatcher() {
            Map<String,Set<StringMatcher>> result = new HashMap<>();
            
            pathsByDataType.forEach( (key,value) -> {
                result.put(key, value.stream().map( path -> new EqualsStringMatcher(path) ).collect(Collectors.toSet()));
            });
            
            pathMatchersByDataType.forEach( (key,value) -> {
                result.merge(key, value, (prev,cur) -> { prev.addAll(cur); return prev; } );
            });
            
            return result;
        }

    }
    
    public DataTypeRequirements getDataRequirements(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor) {
        return getDataRequirements(sourceProvider, libraryDescriptor, null);
    }

    
    public DataTypeRequirements getDataRequirements(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor, Set<String> expressions) {
        AnyColumnVisitor visitor = new AnyColumnVisitor(sourceProvider);
        AnyColumnContext context = new AnyColumnContext();
        
        Library elmLibrary = getElmLibrary(sourceProvider, libraryDescriptor);
        
        // Build a list of all the expressions in the Library that we care about
        List<ExpressionDef> expressionDefs = elmLibrary.getStatements().getDef();
        
        if( expressions != null ) {
            expressionDefs = expressionDefs.stream()
                    .filter(def -> expressions.contains(def.getName()))
                    .collect(Collectors.toList());

            if( expressionDefs.size() != expressions.size() ) {
                throw new IllegalArgumentException(String.format("One or more requested expressions %s not found in library %s", expressions, libraryDescriptor.toString()));
            }
        }
        
        visitor.enterLibrary(elmLibrary.getIdentifier());
        try {
            for( ExpressionDef expressionDef : expressionDefs ) {
                visitor.visitElement(expressionDef, context);
            }
        } finally {
            visitor.exitLibrary();
        }
        
        Map<String,Set<String>> pathsByDataType = new HashMap<>();
        for( Map.Entry<QName, Set<String>> entry : context.getPathsByQName().entrySet() ) {
            if( ! entry.getKey().getNamespaceURI().equals(ElmUtils.SYSTEM_MODEL_URI) ) {
                Set<String> paths = pathsByDataType.computeIfAbsent(entry.getKey().getLocalPart(), key -> new HashSet<>() );
                paths.addAll(entry.getValue());
            }
        }
        
        return new DataTypeRequirements(pathsByDataType, context.getMatchers());
    }
    
    public Map<String,Set<String>> getPathRequirementsByDataType(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor) {
        return getDataRequirements(sourceProvider, libraryDescriptor).getPathsByDataType();
    }
    
    public Map<String,Set<String>> getPathRequirementsByDataType(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor, Set<String> expressions) {
        return getDataRequirements(sourceProvider, libraryDescriptor, expressions).getPathsByDataType();
    }
    
    public Map<String,Set<StringMatcher>> getAnyColumnRequirementsByDataType(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor) {
        return getDataRequirements(sourceProvider, libraryDescriptor).getPathMatchersByDataType();
    }
    
    public Map<String,Set<StringMatcher>> getAnyColumnRequirementsByDataType(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor, Set<String> expressions) {
        return getDataRequirements(sourceProvider, libraryDescriptor, expressions).getPathMatchersByDataType();
    }
    

    protected Library getElmLibrary(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor) {
        CqlLibraryDescriptor targetFormat = new CqlLibraryDescriptor()
                .setLibraryId(libraryDescriptor.getLibraryId())
                .setVersion(libraryDescriptor.getVersion())
                .setFormat(Format.ELM);
        
        CqlLibrary library = sourceProvider.getLibrary(targetFormat);
        if( library == null ) {
            throw new IllegalArgumentException("Failed to load library " + libraryDescriptor.toString());
        }
        
        Library elmLibrary = JAXB.unmarshal(library.getContentAsStream(), Library.class);
        return elmLibrary;
    }
}
