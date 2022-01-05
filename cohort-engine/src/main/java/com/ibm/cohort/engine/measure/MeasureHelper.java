/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import org.hl7.fhir.r4.model.Measure;

public class MeasureHelper {
	
	/**
	 * Provide a search path for resolving a FHIR Measure resource from the given string. The search
	 * path tries to understand the provided resourceID first as a direct mapping to a FHIR resource
	 * and, if not, it tries to resolve it as the canonical URL of a FHIR resource.
	 * 
	 * @param resourceID String matching one of the expected lookup strategies
	 * @param resolver Resolver implementation for the various lookup strategies
	 * @return Resolved FHIR Measure resource
	 */
	public static Measure loadMeasure(String resourceID, FhirResourceResolver<Measure> resolver ) {
		Measure result = null;
		
		if( resourceID != null ) {
			if( resourceID.startsWith("Measure/") || ! resourceID.contains("/") ) {
				result = resolver.resolveById(resourceID.replace("Measure/", ""));
			} else if( resourceID.contains("/Measure/") ) {
				result = resolver.resolveByCanonicalUrl(resourceID);
			}
		}
		
		if( result == null ) {
			throw new IllegalArgumentException(String.format("Failed to resolve Measure resource '%s'", resourceID));
		}
		
		return result;
	}

	public static Measure loadMeasure(Identifier identifier, String version, FhirResourceResolver<Measure> resolver) {
		Measure result;

		result = resolver.resolveByIdentifier(identifier.getValue(), identifier.getSystem(), version);

		if ( result == null ) {
			throw new IllegalArgumentException(String.format("Failed to resolve Measure resource with identifier:'%s', version:'%s'", identifier, version));
		}

		return result;
	}

	public static Measure loadMeasure(MeasureContext context, FhirResourceResolver<Measure> resolver) {
		Measure result = null;

		if (context.getMeasureId() != null) {
			result = loadMeasure(context.getMeasureId(), resolver);
		} else if (context.getIdentifier() != null) {
			result = loadMeasure(context.getIdentifier(), context.getVersion(), resolver);
		}

		if (result == null) {
			throw new IllegalArgumentException(String.format("Failed to resolve Measure: %s", context));
		}

		return result;
	}
}
