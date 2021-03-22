/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.evidence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasureEvidenceHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(MeasureEvidenceHelper.class);

	private static final String LIBRARY_CONCAT = "_";
	private static final String DEFINE_CONCAT = ".";
	
	public static String createEvidenceKey(VersionedIdentifier libraryId, String defineName) {
		return new StringBuilder()
				.append(libraryId.getId())
				.append(LIBRARY_CONCAT)
				.append(libraryId.getVersion())
				.append(DEFINE_CONCAT)
				.append(defineName)
				.toString();
	}
	
	public static List<Type> getFhirTypes(Object value) {
		List<Type> types = new ArrayList<>();
		
		if(value instanceof Iterable) {
			for(Object item : (Iterable<?>)value) {
				if(item instanceof Iterable) {
					types.addAll(getFhirTypes(item));
				}
				else {
					Type type = getFhirType(item);
					
					if(type != null) {
						types.add(type);
					}
				}
			}
		}
		else {
			Type type = getFhirType(value);
			
			if(type != null) {
				types.add(type);
			}
		}
		
		return types;
	}
	
	public static Type getFhirType(Object value) {
		
		if(value instanceof Boolean) {
			return new BooleanType((Boolean)value);
		}
		else if(value instanceof String) {
			return new StringType((String)value);
		}
		else if(value instanceof Double) {
			return new DecimalType((Double)value);
		}
		else if(value instanceof Date) {
			return new DateTimeType((Date)value);
		}
		else if(value instanceof Integer) {
			return new IntegerType((Integer)value);
		}
		else if(value instanceof DomainResource) {
			return new Reference((DomainResource)value);
		}
		else if(value instanceof CodeableConcept) {
			return (CodeableConcept)value;
		}
		else if(value instanceof Annotation) {
			return (Annotation)value;
		}
		else if(value instanceof Attachment) {
			return (Attachment)value;
		}
		else if(value instanceof Period) {
			return (Period)value;
		}
		else if(value instanceof Quantity) {
			return (Quantity)value;
		}
		else if(value instanceof RelatedArtifact) {
			return (RelatedArtifact)value;
		}
		else {
			logger.warn("Unsupported exception type: {}", value);
			return null; 
		}
		
	}
}
