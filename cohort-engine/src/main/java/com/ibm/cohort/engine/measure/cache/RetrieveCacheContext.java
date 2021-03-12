/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import javax.cache.Cache;
import java.io.Closeable;

public interface RetrieveCacheContext extends Closeable {

	Cache<CacheKey, Iterable<Object>> getCache(String contextId);

}
