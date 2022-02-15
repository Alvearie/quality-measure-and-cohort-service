/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXB;

import com.ibm.cohort.cql.library.Format;
import org.cqframework.cql.cql2elm.NamespaceManager;
import org.cqframework.cql.elm.visiting.ElmBaseLibraryVisitor;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.ExpressionRef;
import org.hl7.elm.r1.FunctionRef;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;

/**
 * Provides basic functionality for visiting an ELM library with reference
 * traversal enabled for expression and function references. Additional support
 * could be added for traversal of other types of references, such as
 * terminology resources or parameters, but those are not necessary
 * functionality for the problems we are trying to solve right now.
 * 
 * @param <R> Result type for each visit operation
 * @param <C> Context object that maintains the state of the overall visit
 */
public class GraphWalkingElmVisitor<R, C> extends ElmBaseLibraryVisitor <R, C> {
    
    private static final Logger LOG = LoggerFactory.getLogger(GraphWalkingElmVisitor.class);
    
    private Deque<VersionedIdentifier> libraryStack = new ArrayDeque<>();
    private Set<Element> visited = new HashSet<>();
    private CqlLibraryProvider libraryProvider;
    
    public GraphWalkingElmVisitor(CqlLibraryProvider libraryProvider) {
        this.libraryProvider = libraryProvider;
    }
    
    public void enterLibrary(VersionedIdentifier libraryIdentifier) {
        if (libraryIdentifier == null) {
            throw new IllegalArgumentException("Library Identifier must be provided");
        }
        libraryStack.push(libraryIdentifier);
    }
    public void exitLibrary() {
        libraryStack.pop();
    }
    public VersionedIdentifier getCurrentLibraryIdentifier() {
        if (libraryStack.isEmpty()) {
            throw new IllegalArgumentException("Not in a library context");
        }

        return libraryStack.peek();
    }
    
    @Override
    public R visitLibrary(Library elm, C context) {
        enterLibrary(elm.getIdentifier());
        try {
            return super.visitLibrary(elm, context);
        } finally {
            exitLibrary();
        }
    }
    
    @Override
    public R visitExpressionRef(ExpressionRef ref, C context) {
        R result;
        
        if( ref instanceof FunctionRef ) {
            result = visitFunctionRef((FunctionRef)ref, context);
        } else {
            result = visitRef(ref, context, defaultResult(ref, context));
        }
        
        return result;
    }

    @Override
    public R visitFunctionRef(FunctionRef ref, C context) {
        R result = super.visitFunctionRef(ref, context);
        
        return visitRef(ref, context, result);
    }
    
    protected R visitRef(ExpressionRef ref, C context, R defaultResult) {
    	R result = defaultResult;
    	if( ! visited.contains(ref) ) {
            visited.add(ref);
            
            Library library = prepareLibraryVisit(getCurrentLibraryIdentifier(), ref.getLibraryName(), context);
            try {
                // The TranslatedLibrary class that the CqlTranslator produces has this already indexed. If we want to 
                // use any ELM without relying on the translator, we need to do the lookup ourselves. It is worth
                // considering whether or not we build our own indexing helper instead.
            	Optional<ExpressionDef> optionalEd = library.getStatements().getDef().stream().filter( def -> def.getName().equals(ref.getName()) ).findAny();
            	
            	if(optionalEd.isPresent()) {
            		result = visitElement(optionalEd.get(), context);
            	}
            	else {
            		throw new IllegalArgumentException("Could not find definition for reference " + ref.getName());
            	}
            } finally {
                unprepareLibraryVisit(ref.getLibraryName());
            }
        }
        return result;
    }
    
    protected Library prepareLibraryVisit(VersionedIdentifier libraryIdentifier, String localLibraryName, C context) {
        Library targetLibrary = resolveLibrary(libraryIdentifier);
        if (localLibraryName != null) {
            if( targetLibrary.getIncludes() != null ) {
                Optional<IncludeDef> optional = targetLibrary.getIncludes().getDef().stream().filter(def -> def.getLocalIdentifier().equals(localLibraryName)).findAny();
                if( optional.isPresent() ) {
                    IncludeDef includeDef = optional.get();
                    if (!visited.contains(includeDef)) {
                        super.visitElement(includeDef, context);
                    }
                    targetLibrary = resolveLibraryFromIncludeDef(includeDef);
                }
            }
            enterLibrary(targetLibrary.getIdentifier());
        }
        return targetLibrary;
    }
    
    protected void unprepareLibraryVisit(String localLibraryName) {
        if (localLibraryName != null) {
            exitLibrary();
        }
    }
    
    protected Library resolveLibrary(VersionedIdentifier libraryIdentifier) {
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor().setLibraryId(libraryIdentifier.getId())
                .setVersion(libraryIdentifier.getVersion()).setFormat(Format.ELM);
        
        CqlLibrary library = libraryProvider.getLibrary(descriptor);
        if( library == null ) {
            throw new IllegalArgumentException("Missing library " + descriptor.toString());
        }
        LOG.trace(library.getContent());
        Library elmLibrary = JAXB.unmarshal(library.getContentAsStream(), Library.class);
        return elmLibrary;
    }
    
    public Library resolveLibraryFromIncludeDef(IncludeDef includeDef) {
        VersionedIdentifier targetLibraryIdentifier = new VersionedIdentifier()
                .withSystem(NamespaceManager.getUriPart(includeDef.getPath()))
                .withId(NamespaceManager.getNamePart(includeDef.getPath()))
                .withVersion(includeDef.getVersion());

        return resolveLibrary(targetLibraryIdentifier);
    }
}
