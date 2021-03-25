/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import java.io.IOException;
import java.util.UUID;

/**
 * <p>A simple implementation of {@link RetrieveCacheContext} that will
 * call {@link #flushCache()} whenever a contextId different from the
 * previously used contextId is passed in.
 *
 * <p>Flushing the cache clears all contents in the cache
 * and the currently stored contextId.
 *
 * <p><b>NOTE</b>: The last contextId passed in will not be flushed
 * automatically.  The user will either need to call {@link #flushCache()}
 * or {@link #close()}.
 */
public class DefaultRetrieveCacheContext implements RetrieveCacheContext {

	private static final String CACHE_ID_PREFIX = "default-retrieve-cache-";

	// Create a unique cacheId to prevent conflicts with other caches in the same JVM.
	private static String createCacheId() {
		String uuid = UUID.randomUUID().toString();
		return CACHE_ID_PREFIX + uuid;
	}

	private static CompleteConfiguration<RetrieveCacheKey, Iterable<Object>> getDefaultConfiguration() {
		return new MutableConfiguration<RetrieveCacheKey, Iterable<Object>>()
				// Disable store-by-value by default to prevent needless object copying
				.setStoreByValue(false);
	}

	private final Cache<RetrieveCacheKey, Iterable<Object>> cache;

	private String currentContextId;

	/**
	 * <p>Creates a new context that wraps a new cache created using the system's default JCache provider and configuration.
	 *
	 * <p>The default configuration will have `store-by-value` set to false for performance reasons, but all other
	 * properties will be decided by the chosen JCache provider.
	 */
	public DefaultRetrieveCacheContext() {
		this(getDefaultConfiguration());
	}

	/**
	 * Creates a new context that wraps a new cache created using the system's default JCache provider.
	 * @param config The configuration for the newly created cache.
	 */
	public DefaultRetrieveCacheContext(CompleteConfiguration<RetrieveCacheKey, Iterable<Object>> config) {
		this(Caching.getCachingProvider().getCacheManager().createCache(createCacheId(), config));
	}

	/**
	 * Creates a new context that simply wraps the provided {@link Cache}.
	 * @param cache The underlying {@link Cache}
	 */
	public DefaultRetrieveCacheContext(Cache<RetrieveCacheKey, Iterable<Object>> cache) {
		this.cache = cache;
	}

	@Override
	public Cache<RetrieveCacheKey, Iterable<Object>> getCache(String contextId) {
		if (!contextId.equals(currentContextId)) {
			if (currentContextId != null) {
				flushCache();
			}
			currentContextId = contextId;
		}

		return cache;
	}

	@Override
	public void flushCache() {
		cache.clear();
		currentContextId = null;
	}

	@Override
	public void close() throws IOException {
		flushCache();
		cache.close();
	}

}
