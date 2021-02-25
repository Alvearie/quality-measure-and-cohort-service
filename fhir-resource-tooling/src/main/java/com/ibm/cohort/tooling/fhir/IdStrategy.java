/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import org.hl7.fhir.r4.model.MetadataResource;

/**
 * An IdStrategy provides a mechanism for plugging in an algorithm
 * that will generate a unique ID for a given resource based on
 * the contents of that resource. 
 */
@FunctionalInterface
public interface IdStrategy {
	/**
	 * Generate a unique ID for a given FHIR resource
	 * @param resource FHIR resource
	 * @return unique ID
	 * @throws Exception if the algorithm fails in any way.
	 */
	public String generateId(MetadataResource resource) throws Exception;
}
