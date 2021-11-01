/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;

public class ModelUtils {
    public static class TypeNode {
        private QName typeName;
        private Set<TypeNode> parentTypes = new HashSet<>();
        private Set<TypeNode> childTypes = new HashSet<>();
        
        public TypeNode(QName typeName) {
            this.typeName = typeName;
        }
        
        public QName getTypeName() {
            return typeName;
        }
        public void setTypeName(QName typeName) {
            this.typeName = typeName;
        }
        public Set<TypeNode> getParentTypes() {
            return parentTypes;
        }
        public void setParentTypes(Set<TypeNode> parentTypes) {
            this.parentTypes = parentTypes;
        }
        public Set<TypeNode> getChildTypes() {
            return childTypes;
        }
        public void setChildTypes(Set<TypeNode> childTypes) {
            this.childTypes = childTypes;
        }
    }
    
    public static Map<QName,TypeNode> buildTypeMap(ModelInfo modelInfo) {
        Map<QName,TypeNode> typeMap = new HashMap<>();
        
        for( TypeInfo typeInfo : modelInfo.getTypeInfo() ) {
            if( typeInfo instanceof ClassInfo ) {
                ClassInfo classInfo = (ClassInfo) typeInfo;
                
                QName qname = new QName(modelInfo.getUrl(), classInfo.getName());
                TypeNode node = typeMap.computeIfAbsent(qname, key -> new TypeNode(key) );
                
                if( typeInfo.getBaseType() != null ) {
                    QName baseQName = getBaseTypeName(modelInfo, typeInfo);
                    if( baseQName.getNamespaceURI().equals( modelInfo.getUrl() ) ) {
                        TypeNode parentNode = typeMap.computeIfAbsent(baseQName, key -> new TypeNode(key) );
                        parentNode.getChildTypes().add(node);
                        node.getParentTypes().add(parentNode);
                    }
                } 
            }
        }
        
        return typeMap;
    }

    public static QName getBaseTypeName(ModelInfo modelInfo, TypeInfo typeInfo) {
        String prefixURI;
        String baseTypeName;
        
        String [] parts = typeInfo.getBaseType().split("\\.");
        if( parts.length == 1 ) {
            prefixURI = modelInfo.getUrl();
            baseTypeName = parts[0];
        } else if( parts.length == 2 ) {
            prefixURI = getPrefixURI(modelInfo, parts[0]);
            baseTypeName = parts[1];
        } else {
            throw new IllegalArgumentException(String.format("Unexpected baseType value %s found in model %s version %s. Type names should have at most one parent namespace (e.g. namespace.parent)", typeInfo.getBaseType(), modelInfo.getName(), modelInfo.getVersion()));
        }
        
        return new QName(prefixURI, baseTypeName);
    }

    public static String getPrefixURI(ModelInfo modelInfo, String prefix) {
        String prefixURI = null;
        if( prefix.equals(modelInfo.getName()) ) {
            prefixURI = modelInfo.getUrl();
        } else if( CollectionUtils.isNotEmpty( modelInfo.getRequiredModelInfo() ) ) {
            Optional<String> optional = modelInfo.getRequiredModelInfo().stream()
                .filter( req -> req.getName().equals(prefix) )
                .map( req -> req.getUrl() != null ? req.getUrl() : CqlConstants.SYSTEM_MODEL_URI )
                .findAny();
            if( optional.isPresent() ) {
                prefixURI = optional.get();
            }
        }
        
        if( prefixURI == null && prefix.equals(CqlConstants.SYSTEM_MODEL) ) {
            // All of the older model info files (e.g. fhir-modelinfo-4.0.0.xml) use the System model implicitly
            // without declaring it as required.
            prefixURI = CqlConstants.SYSTEM_MODEL_URI;
        }
        
        if( prefixURI == null ) { 
            throw new IllegalArgumentException(String.format("Unable to resolve model URI for prefix %s in model %s version %s", prefix, modelInfo.getName(), modelInfo.getVersion()));
        }
        return prefixURI;
    }
}
