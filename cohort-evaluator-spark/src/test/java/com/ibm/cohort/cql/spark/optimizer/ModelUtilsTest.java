/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;
import javax.xml.namespace.QName;

import org.hamcrest.Matchers;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.ModelSpecifier;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.junit.Test;

import com.ibm.cohort.cql.spark.optimizer.ModelUtils.TypeNode;

public class ModelUtilsTest {
    @Test
    public void testFHIR401Model() throws Exception {
        ModelInfo modelInfo = loadFromClasspath("org/hl7/fhir/fhir-modelinfo-4.0.1.xml");
        
        Map<QName,TypeNode> typeMap = ModelUtils.buildTypeMap(modelInfo);
        assertEquals(931, typeMap.size());
        
        TypeNode node = typeMap.get(new QName(modelInfo.getUrl(), "MoneyQuantity"));
        assertNotNull(node);
        assertEquals("Missing parent type", 1, node.getParentTypes().size());
        assertEquals("Quantity", node.getParentTypes().iterator().next().getTypeName().getLocalPart());
        
        node = typeMap.get(new QName(modelInfo.getUrl(), "Quantity"));
        assertNotNull(node);
        assertEquals(6, node.getChildTypes().size());
        assertEquals(1, node.getChildTypes().stream().filter( n -> n.getTypeName().getLocalPart().equals("MoneyQuantity")).collect(Collectors.counting()).longValue());
    }
    
    @Test
    public void testFHIR400Model() throws Exception {
        ModelInfo modelInfo = loadFromClasspath("org/hl7/fhir/fhir-modelinfo-4.0.0.xml");
        
        Map<QName,TypeNode> typeMap = ModelUtils.buildTypeMap(modelInfo);
        assertEquals(892, typeMap.size());
        
        TypeNode node = typeMap.get(new QName(modelInfo.getUrl(), "MoneyQuantity"));
        assertNotNull(node);
        assertEquals("Missing parent type", 1, node.getParentTypes().size());
        assertEquals("Quantity", node.getParentTypes().iterator().next().getTypeName().getLocalPart());
        
        node = typeMap.get(new QName(modelInfo.getUrl(), "Quantity"));
        assertNotNull(node);
        assertEquals(5, node.getChildTypes().size());
        assertEquals(1, node.getChildTypes().stream().filter( n -> n.getTypeName().getLocalPart().equals("MoneyQuantity")).collect(Collectors.counting()).longValue());
        
        node = typeMap.get(new QName(modelInfo.getUrl(), "Element"));
        assertNotNull(node);
        // The actual parent is System.Any, but we exclude included types from the type map
        assertEquals(0, node.getParentTypes().size());
    }
    
    @Test
    public void testAllTypesModel() throws Exception {
        ModelInfo modelInfo = JAXB.unmarshal(new File("src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml"), ModelInfo.class);
        
        Map<QName,TypeNode> typeMap = ModelUtils.buildTypeMap(modelInfo);
        assertEquals(6, typeMap.size());
        
        TypeNode node = typeMap.get(new QName(modelInfo.getUrl(), "B"));
        assertNotNull(node);
        assertEquals("Missing parent type", 1, node.getParentTypes().size());
        assertEquals("ChildBase", node.getParentTypes().iterator().next().getTypeName().getLocalPart());
        
        node = typeMap.get(new QName(modelInfo.getUrl(), "ChildBase"));
        assertNotNull(node);
        assertEquals(3, node.getChildTypes().size());
        assertEquals(new HashSet<>(Arrays.asList("B", "C", "D")), node.getChildTypes().stream().map( n -> n.getTypeName().getLocalPart() ).collect(Collectors.toSet()));
    }
    
    @Test
    public void testGetBaseTypeNameNoNamespacePrefix() {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("Dummy");
        modelInfo.setUrl("urn:oid:Dummy");
        
        ClassInfo typeInfo = new ClassInfo();
        typeInfo.setName("MyType");
        typeInfo.setBaseType("BaseType");

        QName qname = ModelUtils.getBaseTypeName(modelInfo, typeInfo);
        assertEquals(new QName(modelInfo.getUrl(), "BaseType"), qname);
    }
    
    @Test
    public void testGetBaseTypeNameSystemNamespacePrefix() {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("Dummy");
        modelInfo.setUrl("urn:oid:Dummy");
        
        ClassInfo typeInfo = new ClassInfo();
        typeInfo.setName("MyType");
        typeInfo.setBaseType("System.BaseType");

        QName qname = ModelUtils.getBaseTypeName(modelInfo, typeInfo);
        assertEquals(new QName(CqlConstants.SYSTEM_MODEL_URI, "BaseType"), qname);
    }
    
    @Test
    public void testGetBaseTypeNameIncludedNamespacePrefixValid() {
        ModelSpecifier otherModel = new ModelSpecifier()
                .withName("Other")
                .withVersion("1.2.3")
                .withUrl("urn:oid:Other");
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("Dummy");
        modelInfo.setUrl("urn:oid:Dummy");
        modelInfo.getRequiredModelInfo().add(otherModel);
        
        ClassInfo typeInfo = new ClassInfo();
        typeInfo.setName("MyType");
        typeInfo.setBaseType("Other.BaseType");

        QName qname = ModelUtils.getBaseTypeName(modelInfo, typeInfo);
        assertEquals(new QName(otherModel.getUrl(), "BaseType"), qname);
    }
    
    @Test
    public void testGetBaseTypeNameIncludedNamespacePrefixMissing() {
        ModelSpecifier otherModel = new ModelSpecifier()
                .withName("Other")
                .withVersion("1.2.3")
                .withUrl("urn:oid:Other");
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("Dummy");
        modelInfo.setVersion("5.4.3");
        modelInfo.setUrl("urn:oid:Dummy");
        modelInfo.getRequiredModelInfo().add(otherModel);
        
        ClassInfo typeInfo = new ClassInfo();
        typeInfo.setName("MyType");
        typeInfo.setBaseType("Missing.BaseType");

        IllegalArgumentException iex = assertThrows(IllegalArgumentException.class, () ->  ModelUtils.getBaseTypeName(modelInfo, typeInfo) );
        assertTrue(iex.getMessage(), iex.getMessage().contains("Missing"));
        assertTrue(iex.getMessage(), iex.getMessage().contains(modelInfo.getName()));
        assertTrue(iex.getMessage(), iex.getMessage().contains(modelInfo.getVersion()));
    }

    @Test
    public void testGetBaseTypeNameInvalidNamespacePrefix() {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("Dummy");
        modelInfo.setVersion("2.1.3");
        modelInfo.setUrl("urn:oid:Dummy");
        
        ClassInfo typeInfo = new ClassInfo();
        typeInfo.setName("MyType");
        typeInfo.setBaseType("System.Child.BaseType");

        IllegalArgumentException iex = assertThrows(IllegalArgumentException.class, () ->  ModelUtils.getBaseTypeName(modelInfo, typeInfo) );
        assertTrue(iex.getMessage().contains("System.Child.BaseType"));
        assertTrue(iex.getMessage().contains(modelInfo.getName()));
        assertTrue(iex.getMessage(), iex.getMessage().contains(modelInfo.getVersion()));
    }
    
    @Test
    public void testGetBaseTypeNameNamespacePrefixNotFound() {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("Dummy");
        modelInfo.setVersion("1.1.1");
        modelInfo.setUrl("urn:oid:Dummy");
        
        ClassInfo typeInfo = new ClassInfo();
        typeInfo.setName("MyType");
        typeInfo.setBaseType("Other.BaseType");

        IllegalArgumentException iex = assertThrows(IllegalArgumentException.class, () ->  ModelUtils.getBaseTypeName(modelInfo, typeInfo) );
        assertTrue(iex.getMessage(), iex.getMessage().contains("Other"));
        assertTrue(iex.getMessage(), iex.getMessage().contains(modelInfo.getName()));
        assertTrue(iex.getMessage(), iex.getMessage().contains(modelInfo.getVersion()));
    }

    @Test
    public void testGetBaseTypeWithoutBaseType() {
        ModelInfo modelInfo = new ModelInfo();
        ClassInfo typeInfo = new ClassInfo();
        typeInfo.setName("MyType");

        assertNull(ModelUtils.getBaseTypeName(modelInfo, typeInfo));
    }

    @Test
    public void testGetChoiceTypesNone() {
        ModelInfo modelInfo = JAXB.unmarshal(new File("src/test/resources/abstract-context/modelinfo/abstract-modelinfo-1.0.0.xml"), ModelInfo.class);
        TypeInfo typeInfo = modelInfo.getTypeInfo().stream().map(ClassInfo.class::cast)
            .filter(classInfo -> classInfo.getName().equals("Alpha"))
            .findFirst().orElse(null);
        Collection<String> choiceTypes = ModelUtils.getChoiceTypeNames(typeInfo);

        assertThat(choiceTypes, Matchers.empty());
    }

    @Test
    public void testGetChoiceTypesValid() {
        ModelInfo modelInfo = JAXB.unmarshal(new File("src/test/resources/abstract-context/modelinfo/abstract-modelinfo-1.0.0.xml"), ModelInfo.class);
        TypeInfo typeInfo = modelInfo.getTypeInfo().stream().map(ClassInfo.class::cast)
            .filter(classInfo -> classInfo.getName().equals("AlphaNumeric"))
            .findFirst().orElse(null);
        Collection<String> choiceTypes = ModelUtils.getChoiceTypeNames(typeInfo);

        assertThat(choiceTypes, Matchers.containsInAnyOrder("Alpha", "Numeric"));
    }

    protected ModelInfo loadFromClasspath(String resourcePath) throws IOException {
        ModelInfo modelInfo;
        try( InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath) ) {
            assertNotNull("Classpath resource not found", is);
            modelInfo = JAXB.unmarshal(is, ModelInfo.class);
        }
        return modelInfo;
    }
}
