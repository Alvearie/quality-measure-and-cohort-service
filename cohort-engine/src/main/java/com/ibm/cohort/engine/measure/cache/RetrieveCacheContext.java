/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import javax.cache.Cache;

public interface RetrieveCacheContext {

	Cache<CacheKey, Iterable<Object>> newCache(String contextId);

	Cache<CacheKey, Iterable<Object>> getCurrentCache();

	void cleanupCache(String contextId);

}
