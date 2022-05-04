/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure;

import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;

public class MeasureHelper<T> {

	private final FhirResourceResolver<T> resolver;

	public MeasureHelper(FhirResourceResolver<T> resolver) {
		this.resolver = resolver;
	}

	/**
	 * Provide a search path for resolving a FHIR Measure resource from the given string. The search
	 * path tries to understand the provided resourceID first as a direct mapping to a FHIR resource
	 * and, if not, it tries to resolve it as the canonical URL of a FHIR resource.
	 * 
	 * @param resourceID String matching one of the expected lookup strategies
	 * @return Resolved FHIR Measure resource
	 */
	public T loadMeasure(String resourceID) {
		T result = null;
		
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

	public T loadMeasure(Identifier identifier, String version) {
		T result;

		result = resolver.resolveByIdentifier(identifier.getValue(), identifier.getSystem(), version);

		if ( result == null ) {
			throw new IllegalArgumentException(String.format("Failed to resolve Measure resource with identifier:'%s', version:'%s'", identifier, version));
		}

		return result;
	}

	public T loadMeasure(MeasureContext context) {
		T result = null;

		if (context.getMeasureId() != null) {
			result = loadMeasure(context.getMeasureId());
		} else if (context.getIdentifier() != null) {
			result = loadMeasure(context.getIdentifier(), context.getVersion());
		}

		if (result == null) {
			throw new IllegalArgumentException(String.format("Failed to resolve Measure: %s", context));
		}

		return result;
	}
}
