/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import javax.cache.Cache;
import java.io.Closeable;

/**
 * <p>Manages an underlying {@link Cache}.
 * A contextId (e.g. patientId) is required to allow for efficient use of memory.
 *
 * <p>Implementations may attempt to call {@link #flushCache()}
 * whenever sensible, but users may manually flush the cache whenever desired.
 */
public interface RetrieveCacheContext extends Closeable {

	Cache<RetrieveCacheKey, Iterable<Object>> getCache(String contextId);

	void flushCache();

}
