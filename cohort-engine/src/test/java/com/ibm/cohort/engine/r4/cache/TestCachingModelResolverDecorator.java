package com.ibm.cohort.engine.r4.cache;

import java.util.Map;

import javax.cache.Cache;

import org.opencds.cqf.cql.engine.model.ModelResolver;

public class TestCachingModelResolverDecorator extends CachingModelResolverDecorator {
	public TestCachingModelResolverDecorator(ModelResolver modelResolver) {
		super(modelResolver);
	}
	
	public void clearCaches() {
		for (Map<String, Cache<String, Object>> cacheMap : perPackageContextResolutions.values()) {
			for (Cache<String, Object> cache : cacheMap.values()) {
				cache.clear();
			}
		}

		for (Cache<String, Class<?>> cache : perPackageTypeResolutionsByTypeName.values()) {
			cache.clear();
		}

		for (Cache<Class<?>, Class<?>> cache : perPackageTypeResolutionsByClass.values()) {
			cache.clear();
		}
	}
}
