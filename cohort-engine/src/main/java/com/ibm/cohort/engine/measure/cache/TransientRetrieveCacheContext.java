/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import java.io.IOException;
import java.util.UUID;

public class TransientRetrieveCacheContext implements RetrieveCacheContext {

	private static final String CACHE_ID_PREFIX = "transient-retrieve-cache-";

	private final Cache<CacheKey, Iterable<Object>> currentCache;

	private String currentContextId;

	public TransientRetrieveCacheContext(CompleteConfiguration<CacheKey, Iterable<Object>> config) {
		// Create a unique cacheId to prevent conflicts with other caches in the same JVM.
		String uuid = UUID.randomUUID().toString();
		String cacheId = CACHE_ID_PREFIX + uuid;
		currentCache = Caching.getCachingProvider().getCacheManager().createCache(cacheId, config);
	}

	@Override
	public Cache<CacheKey, Iterable<Object>> getCache(String contextId) {
		if (!contextId.equals(currentContextId)) {
			if (currentContextId != null) {
				flushCache();
			}
			currentContextId = contextId;
		}

		return currentCache;
	}

	@Override
	public void flushCache() {
		currentCache.clear();
		currentContextId = null;
	}

	@Override
	public void close() throws IOException {
		currentCache.close();
	}

}
