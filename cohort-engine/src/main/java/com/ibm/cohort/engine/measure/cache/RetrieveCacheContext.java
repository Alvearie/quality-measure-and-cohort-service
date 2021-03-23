/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import com.github.benmanes.caffeine.cache.Cache;

public interface RetrieveCacheContext {

	Cache<CacheKey, Iterable<Object>> newCache(String patientId);

	Cache<CacheKey, Iterable<Object>> getCurrentCache();

	// TODO: Is this where the cache would be persisted to a datastore???
	void cleanupCache();

}
