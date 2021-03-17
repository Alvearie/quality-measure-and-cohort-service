/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.cache.Cache;

public class TransientRetrieveCacheContextTest {

	@Test
	public void getCache_withConcreteImplementation() throws Exception {
		CaffeineConfiguration<CacheKey, Iterable<Object>> config = new CaffeineConfiguration<>();
		try(RetrieveCacheContext context = new TransientRetrieveCacheContext(config)) {
			Cache<CacheKey, Iterable<Object>> cache = context.getCache("1");
			Assert.assertNotNull(cache);
		}
	}

	@Test
	public void getCache_sameContextId_singleCall() {
		Cache<CacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new TransientRetrieveCacheContext(expected);
		Assert.assertSame(expected, context.getCache("1"));

		Mockito.verify(expected, Mockito.times(0)).clear();
	}

	@Test
	public void getCache_sameContextId_multipleCall() {
		Cache<CacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new TransientRetrieveCacheContext(expected);
		for (int i = 0; i < 10; i++) {
			Assert.assertSame(expected, context.getCache("1"));
		}

		Mockito.verify(expected, Mockito.times(0)).clear();
	}

	@Test
	public void getCache_differentContextId_singleCall() {
		Cache<CacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new TransientRetrieveCacheContext(expected);
		Assert.assertSame(expected, context.getCache("1"));
		Assert.assertSame(expected, context.getCache("2"));

		Mockito.verify(expected, Mockito.times(1)).clear();
	}

	@Test
	public void getCache_differentContextId_multipleCall() {
		Cache<CacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new TransientRetrieveCacheContext(expected);
		int numIterations = 10;
		for (int i = 0; i < numIterations; i++) {
			Assert.assertSame(expected, context.getCache(Integer.toString(i)));
		}

		Mockito.verify(expected, Mockito.times(numIterations - 1)).clear();
	}

	@Test
	public void getCache_mixedCase() {
		Cache<CacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new TransientRetrieveCacheContext(expected);
		Assert.assertSame(expected, context.getCache("1"));
		Assert.assertSame(expected, context.getCache("1"));
		Assert.assertSame(expected, context.getCache("2"));
		Assert.assertSame(expected, context.getCache("2"));
		Assert.assertSame(expected, context.getCache("2"));
		Assert.assertSame(expected, context.getCache("3"));
		Assert.assertSame(expected, context.getCache("3"));
		Assert.assertSame(expected, context.getCache("4"));
		Assert.assertSame(expected, context.getCache("5"));

		Mockito.verify(expected, Mockito.times(4)).clear();
	}

	@Test
	public void flushCache() {
		Cache<CacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new TransientRetrieveCacheContext(expected);
		context.flushCache();

		Mockito.verify(expected, Mockito.times(1)).clear();
	}

	@Test
	public void close() throws Exception {
		Cache<CacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new TransientRetrieveCacheContext(expected);
		context.close();

		Mockito.verify(expected, Mockito.times(1)).clear();
		Mockito.verify(expected, Mockito.times(1)).close();
	}

}
