/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import org.hl7.fhir.r4.model.Measure;

public class MeasureHelper {
	
	/**
	 * Provide a search path for resolving a FHIR Measure resource from the given string. The search
	 * path tries to understand the provided resourceID first as a direct mapping to a FHIR resource
	 * and, if not, it tries to resolve it as the canonical URL of a FHIR resource.
	 * 
	 * @param resourceID String matching one of the expected lookup strategies
	 * @param provider Resolution implementation for the various lookup strategies
	 * @return Resolved FHIR Measure resource
	 */
	public static Measure loadMeasure(String resourceID, MeasureResolutionProvider<Measure> provider ) {
		Measure result = null;
		if( resourceID.startsWith("Measure/") || ! resourceID.contains("/") ) {
			result = provider.resolveMeasureById(resourceID.replace("Measure/", ""));
		} else if( resourceID.contains("/Measure/") ) {
			result = provider.resolveMeasureByCanonicalUrl(resourceID);
		}
		
		if( result == null ) {
			throw new IllegalArgumentException(String.format("Failed to determine resolution path for provided resourceID '%s'", resourceID));
		}
		
		return result;
	}
}
