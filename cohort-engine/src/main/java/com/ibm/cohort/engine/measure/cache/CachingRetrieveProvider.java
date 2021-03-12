/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;

public class CachingRetrieveProvider implements RetrieveProvider {

	private static final Logger LOG = LoggerFactory.getLogger(CachingRetrieveProvider.class);

	private final RetrieveProvider baseProvider;
	private final RetrieveCacheContext retrieveCacheContext;

	public CachingRetrieveProvider(RetrieveProvider baseProvider, RetrieveCacheContext retrieveCacheContext) {
		this.baseProvider = baseProvider;
		this.retrieveCacheContext = retrieveCacheContext;
	}

	@Override
	public Iterable<Object> retrieve(
			String context,
			String contextPath,
			Object contextValue,
			String dataType,
			String templateId,
			String codePath,
			Iterable<Code> codes,
			String valueSet,
			String datePath,
			String dateLowPath,
			String dateHighPath,
			Interval dateRange
	) {
		Iterable<Object> retVal;

		if (contextValue.getClass() != String.class || datePath != null || dateLowPath != null || dateHighPath != null || dateRange != null) {
			LOG.trace("Skipping cache");
			retVal = baseProvider.retrieve(context, contextPath, contextValue, dataType, templateId, codePath, codes, valueSet, datePath, dateLowPath, dateHighPath, dateRange);
		}
		else {
			Cache<CacheKey, Iterable<Object>> cache = retrieveCacheContext.getCache((String)contextValue);
			LOG.trace("Attempting cache");
			CacheKey key = CacheKey.create(context, contextPath, (String)contextValue, dataType, templateId, codePath, codes, valueSet);

			retVal = cache.get(key);
			if (retVal == null) {
				LOG.trace("Cache miss");
				retVal = baseProvider.retrieve(context, contextPath, contextValue, dataType, templateId, codePath, codes, valueSet, null, null, null, null);
				cache.put(key, retVal);
			}
			else {
				LOG.trace("Cache hit");
			}

		}
		return retVal;
	}

}
