/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import org.hl7.fhir.r4.model.MetadataResource;

@FunctionalInterface
public interface IdStrategy {
	public String generateId(MetadataResource resource) throws Exception;
}
