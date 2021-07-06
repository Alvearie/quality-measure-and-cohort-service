package com.ibm.cohort.engine.r4.cache;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;

import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;

public class CachingR4FhirModelResolver extends R4FhirModelResolver {

	private static final String CACHE_ID = "default-r4-fhir-model-type-cache";

	private static CompleteConfiguration<String, Class<?>> getDefaultConfiguration() {
		return new MutableConfiguration<String, Class<?>>()
				.setStoreByValue(false);
	}

	private static final Cache<String, Class<?>> CACHE = Caching.getCachingProvider().getCacheManager().createCache(CACHE_ID, getDefaultConfiguration());

	public CachingR4FhirModelResolver() {
	}
	
	@Override
	public Class<?> resolveType(String typeName) {
		Class<?> type = CACHE.get(typeName);
		if (type == null) {
			type = super.resolveType(typeName);
			CACHE.put(typeName, type);
		}
		return type;
	}
}
