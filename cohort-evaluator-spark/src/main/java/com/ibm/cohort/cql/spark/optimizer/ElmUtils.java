/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.optimizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.hl7.cql_annotations.r1.CqlToElmError;
import org.hl7.cql_annotations.r1.CqlToElmInfo;
import org.hl7.cql_annotations.r1.ObjectFactory;
import org.hl7.elm.r1.ChoiceTypeSpecifier;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.IntervalTypeSpecifier;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.ListTypeSpecifier;
import org.hl7.elm.r1.NamedTypeSpecifier;
import org.hl7.elm.r1.TupleTypeSpecifier;
import org.hl7.elm.r1.TypeSpecifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class ElmUtils {
    public static final String SYSTEM_MODEL_URI = "urn:hl7-org:elm-types:r1";
    private static final Logger LOG = LoggerFactory.getLogger(ElmUtils.class);

    
    public static Set<QName> getModelTypeNames(Expression expression) {
        Set<QName> modelTypeNames = new HashSet<>();
        if( expression.getResultTypeName() != null ) {
            modelTypeNames.add( expression.getResultTypeName() );
        } else {
            TypeSpecifier resultTypeSpecifier = expression.getResultTypeSpecifier();
            if( resultTypeSpecifier != null ) {
                Set<QName> specifierModelTypeNames = getModelTypeNames(resultTypeSpecifier);
                modelTypeNames.addAll( specifierModelTypeNames );
            } else {
                // This is normal for something like a ConceptRef
                LOG.debug("Could not resolve model type name for expression of type {}", expression.getClass().getSimpleName());
                //throw new IllegalArgumentException("Could not resolve model type name for expression " + expression.getLocator());
            }
        }
        return modelTypeNames;
    }
    
    public static Set<QName> getModelTypeNames(TypeSpecifier resultTypeSpecifier) {
        Set<QName> specifierModelTypeNames = new HashSet<>();
        if( resultTypeSpecifier instanceof NamedTypeSpecifier ) {
            specifierModelTypeNames.add(((NamedTypeSpecifier)resultTypeSpecifier).getName());
        } else if( resultTypeSpecifier instanceof IntervalTypeSpecifier ) {
            specifierModelTypeNames.addAll(getModelTypeNames(((IntervalTypeSpecifier)resultTypeSpecifier).getPointType()));
        } else if( resultTypeSpecifier instanceof ListTypeSpecifier ) {
            specifierModelTypeNames.addAll(getModelTypeNames(((ListTypeSpecifier)resultTypeSpecifier).getElementType()));
        } else if( resultTypeSpecifier instanceof ChoiceTypeSpecifier ) {
            for( TypeSpecifier choice : ((ChoiceTypeSpecifier)resultTypeSpecifier).getChoice() ) {
                specifierModelTypeNames.addAll( getModelTypeNames(choice) );
            }
        } else if( resultTypeSpecifier instanceof TupleTypeSpecifier ) {
            specifierModelTypeNames.add(new QName(SYSTEM_MODEL_URI, "Tuple"));
        } else { 
            throw new IllegalArgumentException("Unknown TypeSpecifier " + resultTypeSpecifier.getClass().getName());
        }
        return specifierModelTypeNames;
    }
    
    /**
     * When a Library is deserialized via the CqlLibraryReader helper class the
     * annotations do not get mapped to Objects. This method helps bridge the gap.
     * 
     * @param library Library object containing unmarshalled annotations
     * @return List of Annotation objects which are usually {@link CqlToElmInfo} or
     *         {@link CqlToElmError} objects.
     */
    public static List<Object> unmarshallAnnotations(Library library) {

        List<Object> annotations = new ArrayList<>();
        if (library.getAnnotation() != null) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
                Unmarshaller u = ctx.createUnmarshaller();
    
                for (Object elem : library.getAnnotation()) {
                    JAXBElement<?> j = (JAXBElement<?>) u.unmarshal((Element) elem);
                    annotations.add(j.getValue());
                }
            } catch( Exception ex ) {
                throw new RuntimeException("Failed to deserialize annotations", ex);
            }
        }
        return annotations;
    }
}
