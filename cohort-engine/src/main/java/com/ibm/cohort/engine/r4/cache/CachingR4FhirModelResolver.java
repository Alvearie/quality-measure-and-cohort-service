package com.ibm.cohort.engine.r4.cache;

import java.util.UUID;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;

import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;

public class CachingR4FhirModelResolver extends R4FhirModelResolver {

	private static final String CACHE_ID_PREFIX = "default-r4-fhir-model-type-cache";

	// Create a unique cacheId to prevent conflicts with other caches in the same JVM.
	private static String createCacheId() {
		String uuid = UUID.randomUUID().toString();
		return CACHE_ID_PREFIX + uuid;
	}

	private static CompleteConfiguration<String, Class<?>> getDefaultConfiguration() {
		return new MutableConfiguration<String, Class<?>>()
				.setStoreByValue(false);
	}

	private static final Cache<String, Class<?>> CACHE = Caching.getCachingProvider().getCacheManager().createCache(createCacheId(), getDefaultConfiguration());

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
