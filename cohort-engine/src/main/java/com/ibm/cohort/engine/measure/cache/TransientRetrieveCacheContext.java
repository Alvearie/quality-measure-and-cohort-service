/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class TransientRetrieveCacheContext implements RetrieveCacheContext {

	private Cache<CacheKey, Iterable<Object>> currentCache;

	@Override
	public Cache<CacheKey, Iterable<Object>> newCache(String patientId) {
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
		this.currentCache = Caffeine.newBuilder()
				.recordStats()
				.maximumSize(1_000)
				.build();
		return currentCache;
	}

	@Override
	public Cache<CacheKey, Iterable<Object>> getCurrentCache() {
		return this.currentCache;
	}

	@Override
	public void cleanupCache() {
		this.currentCache = null;
	}

}
