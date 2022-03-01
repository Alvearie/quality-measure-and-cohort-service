/*
 * (C) Copyright IBM Copr. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.r4.cache;

import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.model.ModelResolver;

public class R4FhirModelResolverFactory {
	/*
	 * Caching the ModelResolver will prevent multiple FhirContext objects
	 * from being created each time one of the methods are called.
	 * This operation should be threadsafe given the way we currently use
	 * these objects.
	 */
	private static final ModelResolver modelResolver = new R4FhirModelResolver();

	public static ModelResolver createCachingResolver() {
		return new CachingModelResolverDecorator(modelResolver);
	}
	
	public static ModelResolver createNonCachingResolver() {
		return modelResolver;
	}
}
