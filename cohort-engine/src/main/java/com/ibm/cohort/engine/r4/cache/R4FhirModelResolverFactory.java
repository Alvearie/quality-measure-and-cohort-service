package com.ibm.cohort.engine.r4.cache;

import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.model.ModelResolver;

public class R4FhirModelResolverFactory {
	public static ModelResolver createCachingResolver() {
		return new CachingModelResolverDecorator(new R4FhirModelResolver());
	}
	
	public static ModelResolver createNonCachingResolver() {
		return new R4FhirModelResolver();
	}
}
