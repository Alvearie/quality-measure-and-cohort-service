/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import org.hl7.fhir.r4.model.Identifier;

/**
 * Defines the interface for resource resolution mechanism for FHIR Measure resources. This could
 * be direct DB, REST, filesystem, zip, etc. as needed by the implementation.
 *
 * @param <MeasureType> specific implementation of Measure resource
 */
public interface MeasureResolutionProvider<MeasureType> {
	/**
	 * Resolve a Measure resource by Resource.id.
	 * @param resourceID resource id value
	 * @return Measure resource or null if no measure found.
	 */
	public MeasureType resolveMeasureById(String resourceID);
	
	/**
	 * Resolve a Measure resource by canonical URL. This allows for FHIR canonical URL syntax
	 * that includes a pipe and version at the end of the URL.
	 * @param url canonical URL
	 * @return Measure resource or null if no measure found.
	 */
	public MeasureType resolveMeasureByCanonicalUrl(String url);
	
	/**
	 * Resolve a Measure resource by name and version. If no version is specified, then
	 * the resource with the newest semantic version is returned.
	 * 
	 * @param name resource name
	 * @param version resource version or null if the latest semantic version is desired.
	 * @return Measure resource or null if no measure found.
	 */
	public MeasureType resolveMeasureByName(String name, String version);
	
	/**
	 * Resolve a measure resource by Resource.identifier and version. If no version is 
	 * specified, then the resource with the newest semantic version is returned.
	 * 
	 * @param identifier Identifier with system and version populated
	 * @param version resource version or null if the latest semantic version is desired.
	 * @return Measure resource or null if no measure found.
	 */
	public MeasureType resolveMeasureByIdentifier(Identifier identifier, String version);
}
