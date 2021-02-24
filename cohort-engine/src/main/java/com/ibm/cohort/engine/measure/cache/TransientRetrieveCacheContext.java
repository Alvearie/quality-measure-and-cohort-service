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
