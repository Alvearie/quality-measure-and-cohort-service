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

import com.ibm.cohort.cql.library.Format;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.UsingDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.elm_modelinfo.r1.ModelInfo;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.spark.optimizer.ModelUtils.TypeNode;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.util.EqualsStringMatcher;
import com.ibm.cohort.cql.util.StringMatcher;

public class DataTypeRequirementsProcessor {
    
    public static class DataTypeRequirements {
        private Map<String, VersionedIdentifier> modelIdByUri;
        private Map<String,Set<String>> pathsByDataType;
        private Map<String,Set<StringMatcher>> pathMatchersByDataType;
        
        public DataTypeRequirements(Map<String,VersionedIdentifier> modelNameByUri, Map<String,Set<String>> pathsByDataType, Map<String,Set<StringMatcher>> pathMatchersByDataType) {
            this.modelIdByUri = modelNameByUri;
            this.pathsByDataType = pathsByDataType;
            this.pathMatchersByDataType = pathMatchersByDataType;
        }
        
        public Map<String,VersionedIdentifier> getModels() {
            return modelIdByUri;
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

    private ModelManager modelManager;
    private Map<String,Map<QName,TypeNode>> typeMapByModelUri = new HashMap<>();

    
    public DataTypeRequirementsProcessor(CqlToElmTranslator cqlTranslator) {
        this.modelManager = cqlTranslator.newModelManager();
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
            for( UsingDef usingDef : elmLibrary.getUsings().getDef() ) {
                visitor.visitElement(usingDef, context);
            }
            
            for( ExpressionDef expressionDef : expressionDefs ) {
                visitor.visitElement(expressionDef, context);
            }
        } finally {
            visitor.exitLibrary();
        }
        
        addToChildTypes(context.getModels(), context.getPathsByQName());
        addToChildTypes(context.getModels(), context.getMatchers());
        
        Map<String,Set<String>> pathsByDataType = mapToLocalPart(context.getPathsByQName());
        Map<String,Set<StringMatcher>> matchersByDataType = mapToLocalPart(context.getMatchers());
        
        return new DataTypeRequirements(context.getModels(), pathsByDataType, matchersByDataType);
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
    
    protected <T> void addToChildTypes(Map<String,VersionedIdentifier> models, Map<QName,Set<T>> valuesByDataType) {
        for( Map.Entry<String,VersionedIdentifier> modelData : models.entrySet() ) {
            Map<QName,TypeNode> typeMap = typeMapByModelUri.computeIfAbsent(modelData.getKey(), key -> {
                ModelInfo modelInfo = modelManager.resolveModel(modelData.getValue()).getModelInfo();
                return ModelUtils.buildTypeMap(modelInfo);
            });
            
            //Create a temporary set and then merge later to avoid concurrent modification
            Map<QName,Set<T>> newValues = new HashMap<>();
            for( Map.Entry<QName,Set<T>> paths : valuesByDataType.entrySet() ) {
                Set<QName> names = getDescendants(typeMap, paths.getKey());
                for( QName name : names ) {
                    Set<T> childValues = newValues.computeIfAbsent(name, key -> new HashSet<>());
                    childValues.addAll(paths.getValue());
                }
            }
            
            for( Map.Entry<QName, Set<T>> entry : newValues.entrySet() ) {
                valuesByDataType.merge(entry.getKey(), entry.getValue(), (oldSet,newSet) -> { oldSet.addAll(newSet); return oldSet; });
            }
        }
    }
    
    protected Set<QName> getDescendants(Map<QName,TypeNode> typeMap, QName self) {
        Set<QName> names = new HashSet<>();
        TypeNode node = typeMap.get(self);
        // node will be null when the type is found in a different model
        if( node != null ) {
            for( TypeNode child : node.getChildTypes() ) {
                names.add(child.getTypeName());
                names.addAll(getDescendants(typeMap, child.getTypeName()));
            }
        }
        return names;
    }
    
    protected <T> Map<String,Set<T>> mapToLocalPart(Map<QName,Set<T>> inputData) {
        Map<String,Set<T>> outputData = new HashMap<>();
        for( Map.Entry<QName, Set<T>> entry : inputData.entrySet() ) {
            if( ! entry.getKey().getNamespaceURI().equals(CqlConstants.SYSTEM_MODEL_URI) ) {
                Set<T> paths = outputData.computeIfAbsent(entry.getKey().getLocalPart(), key -> new HashSet<>() );
                paths.addAll(entry.getValue());
            }
        }
        return outputData;
    }
}
