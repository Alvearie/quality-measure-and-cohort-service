/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import com.ibm.cohort.cql.data.CachingModelResolverDecorator;
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
