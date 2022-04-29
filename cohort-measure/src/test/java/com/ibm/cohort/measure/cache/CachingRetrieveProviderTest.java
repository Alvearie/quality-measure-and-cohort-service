/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.cache;

import com.ibm.cohort.cql.cache.CachingRetrieveProvider;
import com.ibm.cohort.cql.cache.RetrieveCacheContext;
import com.ibm.cohort.cql.cache.RetrieveCacheKey;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Interval;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CachingRetrieveProviderTest {

	private static final String CONTEXT = "context";
	private static final String CONTEXT_PATH = "contextPath";
	private static final String CONTEXT_VALUE = "contextValue";
	private static final String DATA_TYPE = "dataType";
	private static final String TEMPLATE_ID = "templateId";
	private static final String CODE_PATH = "codePath";
	private static final List<Code> CODES = Arrays.asList(
			createCode("code1"),
			createCode("code2"),
			createCode("code3")
	);
	private static final String VALUE_SET = "valueSet";
	private static final String DATE_PATH = "datePath";
	private static final String DATE_LOW_PATH = "dateLowPath";
	private static final String DATE_HIGH_PATH = "dateHighPath";
	private static final Interval DATE_RANGE = new Interval(1, false, 10, false);

	private static Code createCode(String code) {
		return new Code()
				.withCode(code)
				.withSystem(code + "-system")
				.withDisplay(code + "-display")
				.withVersion(code + "version");
	}

	@Test
	public void retrieve_cacheMiss() {
		Iterable<Object> expected = new ArrayList<>();

		RetrieveProvider mockProvider = Mockito.mock(RetrieveProvider.class);
		Mockito.when(mockProvider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, null, null))
				.thenReturn(expected);

		RetrieveCacheContext mockCacheContext = createMockCacheContext(Mockito.mock(Cache.class));
		CachingRetrieveProvider provider = new CachingRetrieveProvider(mockProvider, mockCacheContext);
		Iterable<Object> actual = provider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, null, null);

		Assert.assertSame(expected, actual);
	}

	@Test
	public void retrieve_cacheHit() {
		Iterable<Object> expected = new ArrayList<>();

		RetrieveProvider mockProvider = Mockito.mock(RetrieveProvider.class);

		RetrieveCacheKey retrieveCacheKey = RetrieveCacheKey.create(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET);
		Cache<RetrieveCacheKey, Iterable<Object>> mockCache = createMockCache(retrieveCacheKey, expected);

		RetrieveCacheContext mockCacheContext = createMockCacheContext(mockCache);
		CachingRetrieveProvider provider = new CachingRetrieveProvider(mockProvider, mockCacheContext);
		Iterable<Object> actual = provider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, null, null);

		Assert.assertSame(expected, actual);
	}

	@Test
	public void retrieve_passthrough_nonStringContextValue() {
		Iterable<Object> expected = new ArrayList<>();

		Object contextValue = new Object();
		RetrieveProvider mockProvider = Mockito.mock(RetrieveProvider.class);
		Mockito.when(mockProvider.retrieve(CONTEXT, CONTEXT_PATH, contextValue, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, null, null))
				.thenReturn(expected);

		RetrieveCacheContext mockCacheContext = createMockCacheContext(Mockito.mock(Cache.class));
		CachingRetrieveProvider provider = new CachingRetrieveProvider(mockProvider, mockCacheContext);
		Iterable<Object> actual = provider.retrieve(CONTEXT, CONTEXT_PATH, contextValue, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, null, null);

		Assert.assertSame(expected, actual);
	}

	@Test
	public void retrieve_passthrough_nonNullDatePath() {
		Iterable<Object> expected = new ArrayList<>();

		RetrieveProvider mockProvider = Mockito.mock(RetrieveProvider.class);
		Mockito.when(mockProvider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, DATE_PATH, null, null, null))
				.thenReturn(expected);

		RetrieveCacheContext mockCacheContext = createMockCacheContext(Mockito.mock(Cache.class));
		CachingRetrieveProvider provider = new CachingRetrieveProvider(mockProvider, mockCacheContext);
		Iterable<Object> actual = provider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, DATE_PATH, null, null, null);

		Assert.assertSame(expected, actual);
	}

	@Test
	public void retrieve_passthrough_nonNullDateLowPath() {
		Iterable<Object> expected = new ArrayList<>();

		RetrieveProvider mockProvider = Mockito.mock(RetrieveProvider.class);
		Mockito.when(mockProvider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, DATE_LOW_PATH, null, null))
				.thenReturn(expected);

		RetrieveCacheContext mockCacheContext = createMockCacheContext(Mockito.mock(Cache.class));
		CachingRetrieveProvider provider = new CachingRetrieveProvider(mockProvider, mockCacheContext);
		Iterable<Object> actual = provider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, DATE_LOW_PATH, null, null);

		Assert.assertSame(expected, actual);
	}

	@Test
	public void retrieve_passthrough_nonNullDateHighPath() {
		Iterable<Object> expected = new ArrayList<>();

		RetrieveProvider mockProvider = Mockito.mock(RetrieveProvider.class);
		Mockito.when(mockProvider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, DATE_HIGH_PATH, null))
				.thenReturn(expected);

		RetrieveCacheContext mockCacheContext = createMockCacheContext(Mockito.mock(Cache.class));
		CachingRetrieveProvider provider = new CachingRetrieveProvider(mockProvider, mockCacheContext);
		Iterable<Object> actual = provider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, DATE_HIGH_PATH, null);

		Assert.assertSame(expected, actual);
	}

	@Test
	public void retrieve_passthrough_nonNullDateRange() {
		Iterable<Object> expected = new ArrayList<>();

		RetrieveProvider mockProvider = Mockito.mock(RetrieveProvider.class);
		Mockito.when(mockProvider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, null, DATE_RANGE))
				.thenReturn(expected);

		RetrieveCacheContext mockCacheContext = createMockCacheContext(Mockito.mock(Cache.class));
		CachingRetrieveProvider provider = new CachingRetrieveProvider(mockProvider, mockCacheContext);
		Iterable<Object> actual = provider.retrieve(CONTEXT, CONTEXT_PATH, CONTEXT_VALUE, DATA_TYPE, TEMPLATE_ID, CODE_PATH, CODES, VALUE_SET, null, null, null, DATE_RANGE);

		Assert.assertSame(expected, actual);
	}

	private RetrieveCacheContext createMockCacheContext(Cache<RetrieveCacheKey, Iterable<Object>> retVal) {
		RetrieveCacheContext mockCacheContext = Mockito.mock(RetrieveCacheContext.class);
		Mockito.when(mockCacheContext.getCache(CONTEXT_VALUE))
				.thenReturn(retVal);
		return mockCacheContext;
	}

	private Cache<RetrieveCacheKey, Iterable<Object>> createMockCache(RetrieveCacheKey input, Iterable<Object> retVal) {
		Cache<RetrieveCacheKey, Iterable<Object>> mockCache = Mockito.mock(Cache.class);
		Mockito.when(mockCache.get(input))
				.thenReturn(retVal);
		return mockCache;
	}

}
