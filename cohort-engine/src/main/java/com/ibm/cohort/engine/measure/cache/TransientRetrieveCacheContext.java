/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;


import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.spi.CachingProvider;

public class TransientRetrieveCacheContext implements RetrieveCacheContext {

	private final CompleteConfiguration<CacheKey, Iterable<Object>> config;
	private Cache<CacheKey, Iterable<Object>> currentCache;

	public TransientRetrieveCacheContext(CompleteConfiguration<CacheKey, Iterable<Object>> config) {
		this.config = config;
	}

	@Override
	public Cache<CacheKey, Iterable<Object>> newCache(String patientId) {
		CachingProvider cachingProvider = Caching.getCachingProvider();
		CacheManager cacheManager = cachingProvider.getCacheManager();

		// TODO: Name cache after patient?  Cleanup would have to know about the patinet id as well (not a problem).
		// TODO: Are the caches global?  Will different cache contexts attempt to alter the same cache if the same name is used?
		currentCache = cacheManager.createCache("asdf", config);

		// TODO: Only create a new cache if the passed in patientId doesn't match the last used patient id?
		// This would allow for multiple one off evaluation calls using the same patient id.
		// Would have to store the "current" patient id.
		//
		// This pattern would introduce a risk with storing the cache.
		// User's would have to know to "cleanup" and persist the cache when using a new patient id.
		// The usecases get complex.
		//
		// We could also add a "close" method or something to ensure the "last" patient is handled correctly.
		// Devs like closing closeables.
		return currentCache;
	}

	@Override
	public Cache<CacheKey, Iterable<Object>> getCurrentCache() {
		return this.currentCache;
	}

	@Override
	public void cleanupCache() {
		// TODO: Implement the proper JCache cleanup procedures
		this.currentCache = null;
	}

}
