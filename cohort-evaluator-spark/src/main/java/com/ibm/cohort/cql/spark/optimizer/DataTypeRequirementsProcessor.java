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

import org.apache.commons.collections.CollectionUtils;
import org.cqframework.cql.elm.requirements.ElmDataRequirement;
import org.cqframework.cql.elm.requirements.ElmRequirement;
import org.cqframework.cql.elm.requirements.ElmRequirements;
import org.cqframework.cql.elm.requirements.ElmRequirementsContext;
import org.cqframework.cql.elm.requirements.ElmRequirementsVisitor;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.Retrieve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.translation.CqlLibrarySourceProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.DefaultCqlLibrarySourceProvider;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;

public class DataTypeRequirementsProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DataTypeRequirementsProcessor.class);
    
    private CqlToElmTranslator translator;

    public DataTypeRequirementsProcessor(CqlToElmTranslator translator) {
        this.translator = translator;
    }
    
    public Map<String,Set<String>> getPathsByDataType(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor) {
        return getPathsByDataType(sourceProvider, libraryDescriptor, null);
    }
    
    // This is private because the expression filtering doesn't work well when we want the inferred data requirements. For example,
    // a define statement that is simple "A and B" will return no inferred data requirements for "A" and "B".
    private Map<String,Set<String>> getPathsByDataType(CqlLibraryProvider sourceProvider, CqlLibraryDescriptor libraryDescriptor, Set<String> expressions) {
        Map<String,Set<String>> pathsByDataType = new HashMap<>();
        
        CqlLibrarySourceProvider clsp = new DefaultCqlLibrarySourceProvider(sourceProvider);
        
        ElmRequirementsVisitor visitor = new CustomElmRequirementsVisitor();
        ElmRequirementsContext context = new ElmRequirementsContext(translator.newLibraryManager(clsp), translator.getOptions(), visitor); 
        
        CqlLibraryDescriptor targetFormat = new CqlLibraryDescriptor()
                .setLibraryId(libraryDescriptor.getLibraryId())
                .setVersion(libraryDescriptor.getVersion())
                .setFormat(Format.ELM);
        
        TranslatingCqlLibraryProvider provider = new TranslatingCqlLibraryProvider(sourceProvider, translator);
        CqlLibrary library = provider.getLibrary(targetFormat);
        if( library == null ) {
            throw new IllegalArgumentException("Failed to load library " + libraryDescriptor.toString());
        }
        
        Library elmLibrary = JAXB.unmarshal(library.getContentAsStream(), Library.class);
        
        // Build a list of all the expressions in the Library that we care about
        List<ExpressionDef> expressionDefs = elmLibrary.getStatements().getDef();
        
        if( expressions != null ) {
            expressionDefs = expressionDefs.stream()
                    .filter(def -> expressions.contains(def.getName()))
                    .collect(Collectors.toList());

            if( expressionDefs.size() != expressions.size() ) {
                throw new IllegalArgumentException(String.format("One or more requested expressions %s not found in library %s", expressions, library.getDescriptor().toString()));
            }
        }
        
        context.enterLibrary(elmLibrary.getIdentifier());
        try {
            for( ExpressionDef expressionDef : expressionDefs ) {
                visitor.visitElement(expressionDef, context);
            }
        } finally {
            context.exitLibrary();
        }
        
        // Collect all the requirements both direct and inferred
        ElmRequirements requirements = new ElmRequirements(elmLibrary.getIdentifier(), elmLibrary);
        requirements.reportRequirement(context.getRequirements());
        // Collect reported data requirements from each expression
        for (ExpressionDef ed : expressionDefs ) {
            ElmRequirements reportedRequirements = context.getReportedRequirements(ed);
            requirements.reportRequirement(reportedRequirements);
            
            ElmRequirement inferredRequirements = context.getInferredRequirements(ed);
            requirements.reportRequirement(inferredRequirements);
        }
        
        //requirements = requirements.collapse();
        
        // Grab all of the object properties
        for( ElmRequirement requirement : requirements.getRetrieves() ) {
            Retrieve retrieve = (Retrieve) requirement.getElement();
            
            if( retrieve.getDataType() != null ) {
                Set<String> properties = pathsByDataType.computeIfAbsent(retrieve.getDataType().getLocalPart(), k -> new HashSet<>());
                CollectionUtils.addIgnoreNull(properties, retrieve.getCodeProperty());
                CollectionUtils.addIgnoreNull(properties, retrieve.getDateProperty());
                CollectionUtils.addIgnoreNull(properties, retrieve.getDateLowProperty());
                CollectionUtils.addIgnoreNull(properties, retrieve.getDateHighProperty());
                
                if( requirement instanceof ElmDataRequirement ) {
                    ElmDataRequirement edm = (ElmDataRequirement) requirement;
                    if( edm.getProperties() != null ) {
                        edm.getProperties().forEach( p -> properties.add( p.getPath() ) );
                    }
                }
            } else { 
                LOG.warn("Found null resultType for " + retrieve.toString());
            }
            
        }
        
        return pathsByDataType;
    }
}
